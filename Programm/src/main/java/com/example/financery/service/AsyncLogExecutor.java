package com.example.financery.service;

public interface AsyncLogExecutor {
    void executeCreateLogs(Long taskId, String date);
}