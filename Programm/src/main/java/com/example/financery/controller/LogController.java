package com.example.financery.controller;

import com.example.financery.exception.InvalidInputException;
import com.example.financery.model.LogObject;
import com.example.financery.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/logs")
@Tag(name = "Логи", description = "API для работы с логами")
public class LogController {

    private final LogService logService;


    public LogController(LogService logService) {
        this.logService = logService;
    }

    @Operation(
            summary = "Скачать лог-файл",
            description = "Возвращает .log файл с записями логов за указанную дату."
    )
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLogFile(
            @Parameter(description = "Дата логов в формате dd-mm-yyyy",
                    required = true, example = "28-04-2025")
            @RequestParam String date) {
        Resource resource = logService.downloadLogs(date);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/create")
    @Operation(summary = "Create log file asynchronously", description = "Starts log"
            + " file generation and returns an ID")
    public ResponseEntity<Long> createLogFile(@RequestParam String date) {
        Long id = logService.createLogAsync(date);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/status/{id}")
    @Operation(summary = "Get log generation status",
            description = "Returns the current status of log file generation by ID")
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable Long id) {
        if (id < 1) {
            throw new InvalidInputException("Id must be greater than 0");
        }
        LogObject logObject = logService.getStatus(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", logObject.getStatus());
        if (logObject.getErrorMessage() != null) {
            response.put("error", logObject.getErrorMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "Download generated log file",
            description = "Downloads the generated log file by ID")
    public ResponseEntity<Resource> getLogFileById(@PathVariable Long id) {
        if (id < 1) {
            throw new InvalidInputException("Id must be greater than 0");
        }
        return logService.downloadCreatedLogs(id);
    }
}