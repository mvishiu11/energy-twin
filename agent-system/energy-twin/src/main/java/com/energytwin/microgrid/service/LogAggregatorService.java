package com.energytwin.microgrid.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

/** Aggregates simulation logs in memory. */
@Service
public class LogAggregatorService {

  private final List<String> logs = new CopyOnWriteArrayList<>();

  /**
   * Adds a log message with a timestamp.
   *
   * @param message the log message.
   */
  public void log(String message) {
    String timestampedMessage = LocalDateTime.now() + ": " + message;
    logs.add(timestampedMessage);
  }

  /**
   * Returns an unmodifiable list of all log messages.
   *
   * @return list of log messages.
   */
  public List<String> getLogs() {
    return Collections.unmodifiableList(logs);
  }
}
