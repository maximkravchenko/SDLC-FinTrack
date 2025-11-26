package com.example.financery.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogObject {
    private Long id;
    private String status;
    private String filePath;
    private String errorMessage;

    public LogObject(Long id, String status) {
        this.id = id;
        this.status = status;
    }
}
