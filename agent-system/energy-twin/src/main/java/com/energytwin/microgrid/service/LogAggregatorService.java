package com.energytwin.microgrid.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

/** Aggregates simulation logs in memory, grouped by agent name. */
@Service
public class LogAggregatorService {

  private final Map<String, List<String>> logsByAgent = new ConcurrentHashMap<>();

  /**
   * Adds a log message for a specific agent.
   *
   * @param agentName the name of the agent (e.g. Battery1, AggregatorAgent)
   * @param message the log message
   */
  public void log(String agentName, String message) {
    String timestampedMessage = LocalDateTime.now() + ": " + message;
    logsByAgent
        .computeIfAbsent(agentName, k -> new CopyOnWriteArrayList<>())
        .add(timestampedMessage);
  }

  /**
   * Returns all logs for all agents.
   *
   * @return unmodifiable map of agent logs
   */
  public Map<String, List<String>> getAllLogs() {
    Map<String, List<String>> copy = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : logsByAgent.entrySet()) {
      copy.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
    }
    return Collections.unmodifiableMap(copy);
  }

  /**
   * Returns logs for a specific agent.
   *
   * @param agentName the agent's name
   * @return unmodifiable list of logs, or empty list if agent not found
   */
  public List<String> getLogsForAgent(String agentName) {
    return Collections.unmodifiableList(
        logsByAgent.getOrDefault(agentName, Collections.emptyList()));
  }

  /** Clears all logs (useful for restarting simulations). */
  public void clearLogs() {
    logsByAgent.clear();
  }
}
