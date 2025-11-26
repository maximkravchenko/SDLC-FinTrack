package com.example.financery.service.impl;

import com.example.financery.exception.FileNotReadyException;
import com.example.financery.exception.InvalidInputException;
import com.example.financery.exception.NotFoundException;
import com.example.financery.model.LogObject;
import com.example.financery.service.AsyncLogExecutor;
import com.example.financery.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class LogServiceImpl implements LogService {

    private final Path logFilePath;
    private final Path tempDir;
    private final AtomicLong idCounter = new AtomicLong(1);
    private Map<Long, LogObject> tasks = new ConcurrentHashMap<>();
    private static final String DATE_FORMAT = "dd-MM-yyyy"; // Обновлено

    private final AsyncLogExecutor asyncLogExecutor;

    public LogServiceImpl(
            @Value("${app.log.file.path}") String logFilePath,
            @Value("${app.temp.dir.path}") String tempDirPath,
            AsyncLogExecutor asyncLogExecutor,
            Map<Long, LogObject> tasks) {
        this.logFilePath = Paths.get(logFilePath);
        this.tempDir = Paths.get(tempDirPath);
        this.asyncLogExecutor = asyncLogExecutor;
        this.tasks = tasks;
        ensureTempDirExists();
    }

    public void ensureTempDirExists() {
        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
                log.info("Создана защищённая временная директория: {}", tempDir);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удаётся создать защищённую временную директорию",
                    e);
        }
    }

    @Override
    public Resource downloadLogs(String date) {
        LocalDate logDate = parseDate(date);
        validateLogFileExists(logFilePath);
        String formattedDate = logDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));

        Path tempFilePath = createTempFile(logDate);
        filterAndWriteLogsToTempFile(logFilePath, formattedDate, tempFilePath);

        Resource resource = createResourceFromTempFile(tempFilePath, date);
        log.info("Файл логов с датой {} успешно загружен", date);
        return resource;
    }

    @Override
    public LocalDate parseDate(String date) {
        log.debug("Получена дата для парсинга: '{}'", date); // Отладочный лог
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            log.debug("Используемый формат: '{}'", DATE_FORMAT); // Лог формата
            return LocalDate.parse(date.trim(), formatter); // Убираем пробелы
        } catch (DateTimeParseException e) {
            log.error("Ошибка парсинга даты: '{}', причина: {}", date, e.getMessage());
            throw new InvalidInputException("Неверный формат даты. Требуется " + DATE_FORMAT);
        }
    }

    @Override
    public void validateLogFileExists(Path path) {
        if (!Files.exists(path)) {
            throw new NotFoundException("Файл не существует: " + path);
        }
    }

    @Override
    public Path createTempFile(LocalDate logDate) {
        try {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                Path tempFilePath = Files.createTempFile(tempDir, "log-" + logDate + "-", ".log");
                java.io.File tempFile = tempFilePath.toFile();
                if (!tempFile.setReadable(true, true)) {
                    throw new IllegalStateException(
                            "Не удалось установить права на чтение для временного файла: "
                                    + tempFile);
                }
                if (!tempFile.setWritable(true, true)) {
                    throw new IllegalStateException(
                            "Не удалось установить права на запись для временного файла: "
                                    + tempFile);
                }
                if (tempFile.canExecute() && !tempFile.setExecutable(false, false)) {
                    log.warn(
                            "Не удалось удалить права на выполнение для временного файла: {}",
                            tempFile);
                }
                log.info("Создан защищённый временный файл на Windows: {}",
                        tempFile.getAbsolutePath());
                return tempFilePath;
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
                java.nio.file.attribute.FileAttribute<Set<PosixFilePermission>> attr =
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions
                                .fromString("rw-------"));
                Path tempFilePath = Files.createTempFile(tempDir,
                        "log-" + logDate + "-", ".log", attr);
                log.info("Создан защищённый временный файл на Unix/Linux: {}",
                        tempFilePath.toAbsolutePath());
                return tempFilePath;
            } else {
                throw new IllegalStateException("Неподдерживаемая ОС: " + osName);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка при создании временного файла: "
                    + e.getMessage());
        }
    }

    @Override
    public void filterAndWriteLogsToTempFile(Path logFilePath,
                                             String formattedDate,
                                             Path tempFilePath) {
        try (BufferedReader reader = Files.newBufferedReader(logFilePath)) {
            Files.write(tempFilePath, reader.lines()
                    .filter(line -> line.contains(formattedDate))
                    .toList());
            log.info(
                    "Отфильтрованные логи за дату {} записаны во временный файл {}",
                    formattedDate, tempFilePath);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Ошибка при обработке файла логов: "
                            + e.getMessage());
        }
    }

    protected Resource createUrlResource(URI uri) throws MalformedURLException {
        return new UrlResource(uri);
    }

    @Override
    public Resource createResourceFromTempFile(Path tempFilePath, String date) {
        try {
            long size = Files.size(tempFilePath);
            log.debug("Размер временного файла {}: {} байт", tempFilePath, size);
            if (size == 0) {
                throw new NotFoundException("Нет логов за указанную дату: " + date);
            }
            Resource resource = createUrlResource(tempFilePath.toUri());
            tempFilePath.toFile().deleteOnExit();
            log.info("Создан загружаемый ресурс из временного файла: {}", tempFilePath);
            return resource;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Ошибка при создании ресурса из временного файла: "
                            + e.getMessage());
        }
    }

    @Override
    public Long createLogAsync(String date) {
        log.debug("Вызов createLogAsync с датой: '{}'", date); // Отладочный лог
        Long id = idCounter.getAndIncrement();
        LogObject logObject = new LogObject(id, "IN_PROGRESS");
        tasks.put(id, logObject);
        asyncLogExecutor.executeCreateLogs(id, date);
        return id;
    }

    @Override
    public LogObject getStatus(Long taskId) {
        LogObject obj = tasks.get(taskId);
        if (obj == null) {
            throw new NotFoundException("Log object not found");
        }
        return obj;
    }

    @Override
    public ResponseEntity<Resource> downloadCreatedLogs(Long taskId) {
        log.info("Запрос на скачивание логов для ID: {}", taskId);

        LogObject logObject = getStatus(taskId);
        if (!"COMPLETED".equals(logObject.getStatus())) {
            log.error("Файл логов не готов. Текущий статус: {}", logObject.getStatus());
            throw new FileNotReadyException(
                    "Файл логов не готов. Текущий статус: " + logObject.getStatus());
        }

        Path path = Paths.get(logObject.getFilePath());
        if (!Files.exists(path)) {
            log.error("Файл логов не существует по пути: {}", path);
            throw new NotFoundException("Файл логов не существует по пути: " + path);
        }

        try {
            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Ошибка при создании ресурса: " + e.getMessage());
        }
    }
}