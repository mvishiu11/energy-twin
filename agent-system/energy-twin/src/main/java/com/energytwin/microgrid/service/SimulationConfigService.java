package com.energytwin.microgrid.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads simulation configuration from an external JSON file or via API and provides utility methods
 * to validate and retrieve the configuration.
 */
@Getter
@Service
public class SimulationConfigService {

  private static final Logger logger = LoggerFactory.getLogger(SimulationConfigService.class);

  private Map<?, ?> config;

  @PostConstruct
  public void init() {
    logger.info("No default simulation configuration loaded. Waiting for startup config via API.");
  }

  /**
   * Sets the simulation configuration from a JSON string. This method parses the provided JSON and
   * updates the internal configuration.
   *
   * @param configJson the simulation configuration as a JSON string.
   */
  public void setConfigFromString(String configJson) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Map<?, ?> newConfig = mapper.readValue(configJson, Map.class);
      if (!newConfig.containsKey("simulation")) {
        throw new IllegalArgumentException("Provided configuration is missing 'simulation' key.");
      }
      this.config = newConfig;
      logger.info("Simulation configuration updated via API: {}", config);
    } catch (Exception e) {
      logger.error("Error parsing simulation configuration from string", e);
      throw new RuntimeException("Error parsing simulation configuration: " + e.getMessage(), e);
    }
  }

  public int getTickIntervalMillis() {
    Object simulationObj = config.get("simulation");
    if (simulationObj == null) {
      throw new IllegalArgumentException("Missing 'simulation' key in configuration.");
    }
    if (!(simulationObj instanceof Map)) {
      throw new IllegalArgumentException(
          "'simulation' is not a Map. Found type: " + simulationObj.getClass().getName());
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> simulationMap = (Map<String, Object>) simulationObj;

    Object intervalObj = simulationMap.get("tickIntervalMillis");
    if (intervalObj == null) {
      throw new IllegalArgumentException(
          "Missing 'tickIntervalMillis' key in simulation configuration.");
    }
    return (int) intervalObj;
  }

  public double getExternalSourceCost() {
    Object simulationObj = config.get("simulation");
    if (simulationObj == null) {
      throw new IllegalArgumentException("Missing 'simulation' key in configuration.");
    }
    if (!(simulationObj instanceof Map)) {
      throw new IllegalArgumentException(
          "'simulation' is not a Map. Found type: " + simulationObj.getClass().getName());
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> simulationMap = (Map<String, Object>) simulationObj;

    Object costObj = simulationMap.get("externalSourceCost");
    if (costObj == null) {
      return 9999.0;
    }
    return (double) costObj;
  }

  public double getExternalSourceCap() {
    Object simulationObj = config.get("simulation");
    if (simulationObj == null) {
      throw new IllegalArgumentException("Missing 'simulation' key in configuration.");
    }
    if (!(simulationObj instanceof Map)) {
      throw new IllegalArgumentException(
          "'simulation' is not a Map. Found type: " + simulationObj.getClass().getName());
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> simulationMap = (Map<String, Object>) simulationObj;

    Object capObj = simulationMap.get("externalSourceCap");
    if (capObj == null) {
      return 9999.0;
    }
    return (double) capObj;
  }

  public int getMetricsPerNTicks(){
    Object simulationObj = config.get("simulation");
    if (simulationObj == null) {
      throw new IllegalArgumentException("Missing 'simulation' key in configuration.");
    }
    if (!(simulationObj instanceof Map)) {
      throw new IllegalArgumentException(
              "'simulation' is not a Map. Found type: " + simulationObj.getClass().getName());
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> simulationMap = (Map<String, Object>) simulationObj;

    Object metricsPerNTick = simulationMap.get("metricsPerNTicks");

    if (metricsPerNTick == null){
      return 2;
    }
    return (int) metricsPerNTick;
  }

  /**
   * Validates and retrieves the list of agent definitions from the simulation configuration.
   *
   * <p>It checks that:
   *
   * <ul>
   *   <li>The configuration contains a "simulation" key whose value is a Map.
   *   <li>The simulation Map contains an "agents" key whose value is a List.
   *   <li>Each element in the list is a Map with at least "type" and "name" as Strings.
   * </ul>
   *
   * @return a List of agent definitions (each a Map of String to Object).
   * @throws IllegalArgumentException if the configuration is missing required keys or values.
   */
  public List<Map<String, Object>> getValidatedAgentDefinitions() throws IllegalArgumentException {
    Object simulationObj = config.get("simulation");
    List<Object> rawAgentsList = getAgentsList(simulationObj);
    List<Map<String, Object>> validAgents = new ArrayList<>();

    for (Object agentObj : rawAgentsList) {
      if (!(agentObj instanceof Map)) {
        throw new IllegalArgumentException(
            "Agent definition is not a Map. Found type: " + agentObj.getClass().getName());
      }
      @SuppressWarnings("unchecked")
      Map<String, Object> agentDef = (Map<String, Object>) agentObj;
      Object typeObj = agentDef.get("type");
      Object nameObj = agentDef.get("name");
      if (!(typeObj instanceof String)) {
        throw new IllegalArgumentException(
            "Agent definition 'type' is not a String. Found type: "
                + (typeObj != null ? typeObj.getClass().getName() : "null"));
      }
      if (!(nameObj instanceof String)) {
        throw new IllegalArgumentException(
            "Agent definition 'name' is not a String. Found type: "
                + (nameObj != null ? nameObj.getClass().getName() : "null"));
      }
      validAgents.add(agentDef);
    }
    return validAgents;
  }

  /**
   * Iterates over the list of agent definitions and returns the definition for an agent that
   * matches the expected type and name.
   *
   * @param expectedType The expected type (e.g., "energySource").
   * @param agentName The name of the agent.
   * @return The agent definition map if found; otherwise, null.
   */
  public Map<String, Object> findAgentDefinition(String expectedType, String agentName) {
    List<Map<String, Object>> agentsList = this.getValidatedAgentDefinitions();
    for (Map<String, Object> agentDef : agentsList) {
      Object typeObj = agentDef.get("type");
      Object nameObj = agentDef.get("name");
      if (typeObj instanceof String type && nameObj instanceof String name) {
        if (expectedType.equalsIgnoreCase(type) && agentName.equals(name)) {
          return agentDef;
        }
      } else {
        logger.error(
            "Warning: 'type' or 'name' field is not a String in agent definition: {}", agentDef);
      }
    }
    return null;
  }

  private static List<Object> getAgentsList(Object simulationObj) {
    if (simulationObj == null) {
      throw new IllegalArgumentException("Missing 'simulation' key in configuration.");
    }
    if (!(simulationObj instanceof Map)) {
      throw new IllegalArgumentException(
          "'simulation' is not a Map. Found type: " + simulationObj.getClass().getName());
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> simulationMap = (Map<String, Object>) simulationObj;

    Object agentsObj = simulationMap.get("agents");
    if (agentsObj == null) {
      throw new IllegalArgumentException("Missing 'agents' key in simulation configuration.");
    }
    if (!(agentsObj instanceof List)) {
      throw new IllegalArgumentException(
          "'agents' is not a List. Found type: " + agentsObj.getClass().getName());
    }
    @SuppressWarnings("unchecked")
    List<Object> rawAgentsList = (List<Object>) agentsObj;
    return rawAgentsList;
  }
}
