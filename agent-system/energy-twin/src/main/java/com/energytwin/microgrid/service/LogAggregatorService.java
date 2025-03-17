package com.energytwin.microgrid.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LogAggregatorService {

    private final List<String> logs = new CopyOnWriteArrayList<>();

    public void log(String message) {
        String timestampedMessage = LocalDateTime.now() + ": " + message;
        logs.add(timestampedMessage);
        System.out.println(timestampedMessage);
    }

    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }
}
