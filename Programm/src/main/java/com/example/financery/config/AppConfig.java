package com.example.financery.config;

import com.example.financery.model.LogObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AppConfig {

    @Bean
    public Map<Long, LogObject> logTasks() {
        return new ConcurrentHashMap<>();
    }
}