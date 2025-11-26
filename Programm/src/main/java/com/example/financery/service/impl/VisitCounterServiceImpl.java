package com.example.financery.service.impl;

import java.util.concurrent.atomic.AtomicLong;

import com.example.financery.service.VisitCounterService;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterServiceImpl implements VisitCounterService {
    private final AtomicLong counter = new AtomicLong(0);

    public void increment() {
        counter.incrementAndGet();
    }

    public Long getCounter() {
        return counter.get();
    }
}
