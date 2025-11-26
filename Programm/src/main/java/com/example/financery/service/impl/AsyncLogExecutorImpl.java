package com.example.financery.service.impl;

import com.example.financery.exception.InvalidInputException;
import com.example.financery.exception.NotFoundException;
import com.example.financery.model.LogObject;
import com.example.financery.service.AsyncLogExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class AsyncLogExecutorImpl implements AsyncLogExecutor {

    private final Path logFilePath;
    private final Path tempDir;
    private final Map<Long, LogObject> tasks;
    private static final String DATE_FORMAT = "yyyy-mm-dd";
    private static final String FAIL_TEXT = "FAILED";

    public AsyncLogExecutorImpl(
            @Value("${app.log.file.path}") String logFilePath,
            @Value("${app.temp.dir.path}") String tempDirPath,
            Map<Long, LogObject> tasks) {
        this.logFilePath = Paths.get(logFilePath);
        this.tempDir = Paths.get(tempDirPath);
        this.tasks = tasks;
        ensureTempDirExists();
    }

    private void ensureTempDirExists() {
        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
                log.info("Создана защищённая временная директория: {}", tempDir);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удаётся создать защищённую временную директорию", e);
        }
    }

    @Async("executor")
    @Override
    public void executeCreateLogs(Long taskId, String date) {
        try {
            Thread.sleep(20000); // Имитация долгой задачи

            LocalDate logDate = parseDate(date);
            validateLogFileExists(logFilePath);
            String formattedDate = logDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));

            List<String> logLines = Files.readAllLines(logFilePath);
            List<String> currentLogs = logLines.stream()
                    .filter(line -> line.contains(formattedDate))
                    .toList();

            if (currentLogs.isEmpty()) {
                LogObject logObject = tasks.get(taskId);
                if (logObject != null) {
                    logObject.setStatus(FAIL_TEXT);
                    logObject.setErrorMessage("Нет логов за дату: " + date);
                }
                throw new NotFoundException("Нет логов за дату: " + date);
            }

            Path logFile = createTempFile(logDate);
            Files.write(logFile, currentLogs);
            logFile.toFile().deleteOnExit();

            LogObject task = tasks.get(taskId);
            if (task != null) {
                task.setStatus("COMPLETED");
                task.setFilePath(logFile.toString());
            }
        } catch (InvalidInputException e) {
            LogObject task = tasks.get(taskId);
            if (task != null) {
                task.setStatus(FAIL_TEXT);
                task.setErrorMessage(e.getMessage());
            }
            log.error("Invalid date format for taskId {}: {}", taskId, e.getMessage());
        } catch (InterruptedException e) {
            LogObject task = tasks.get(taskId);
            if (task != null) {
                task.setStatus(FAIL_TEXT);
                task.setErrorMessage("Task interrupted");
            }
            Thread.currentThread().interrupt();
            log.warn("Task interrupted for taskId {}", taskId);
        } catch (Exception e) {
            LogObject task = tasks.get(taskId);
            if (task != null) {
                task.setStatus(FAIL_TEXT);
                task.setErrorMessage("Unexpected error: " + e.getMessage());
            }
            log.error(
                    "Unexpected error in executeCreateLogs for taskId {}: {}",
                    taskId, e.getMessage());
        }
    }

    private LocalDate parseDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Неверный формат даты. Требуется yyyy-mm-dd");
        }
    }

    private void validateLogFileExists(Path path) {
        if (!Files.exists(path)) {
            throw new NotFoundException("Файл не существует: " + path);
        }
    }

    private Path createTempFile(LocalDate logDate) throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            Path tempFilePath = Files.createTempFile(tempDir, "logs-" + logDate + "-", ".log");
            java.io.File tempFile = tempFilePath.toFile();
            if (!tempFile.setReadable(true, true) || !tempFile.setWritable(true, true)) {
                throw new IllegalStateException(
                        "Не удалось установить права для файла: " + tempFile);
            }
            if (tempFile.canExecute() && !tempFile.setExecutable(false, false)) {
                log.warn("Не удалось удалить права на выполнение для файла: {}", tempFile);
            }
            return tempFilePath;
        } else {
            java.nio.file.attribute.FileAttribute<Set<PosixFilePermission>> attr =
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rw-------"));
            return Files.createTempFile(tempDir, "logs-" + logDate + "-", ".log", attr);
        }
    }
}