package com.energytwin.microgrid.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads simulation configuration from an external JSON file.
 */
@Service
public class SimulationConfigService {

    @Value("classpath:simulation-config.json")
    private Resource configResource;

    @Getter
    private Map<?, ?> config;

    private final Logger logger = LoggerFactory.getLogger(SimulationConfigService.class);

    @PostConstruct
    public void init() {
        try (InputStream is = configResource.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            config = mapper.readValue(is, Map.class);
            System.out.println("Simulation configuration loaded: " + config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load simulation configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Validates and retrieves the list of agent definitions from the simulation configuration.
     * It checks that:
     * <ul>
     *   <li>The configuration contains a "simulation" key whose value is a Map.</li>
     *   <li>The simulation Map contains an "agents" key whose value is a List.</li>
     *   <li>Each element in the list is a Map with at least "type" and "name" as Strings.</li>
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
                throw new IllegalArgumentException("Agent definition is not a Map. Found type: " + agentObj.getClass().getName());
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> agentDef = (Map<String, Object>) agentObj;
            Object typeObj = agentDef.get("type");
            Object nameObj = agentDef.get("name");
            if (!(typeObj instanceof String)) {
                throw new IllegalArgumentException("Agent definition 'type' is not a String. Found type: " +
                        (typeObj != null ? typeObj.getClass().getName() : "null"));
            }
            if (!(nameObj instanceof String)) {
                throw new IllegalArgumentException("Agent definition 'name' is not a String. Found type: " +
                        (nameObj != null ? nameObj.getClass().getName() : "null"));
            }
            validAgents.add(agentDef);
        }
        return validAgents;
    }

    /**
     * Iterates over the list of agent definitions and returns the definition
     * for an agent that matches the expected type and name.
     *
     * @param expectedType The expected type (e.g., "energySource").
     * @param agentName    The name of the agent (usually the result of getLocalName()).
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
                logger.error("Warning: 'type' or 'name' field is not a String in agent definition: {}", agentDef);
            }
        }
        return null;
    }

    private static List<Object> getAgentsList(Object simulationObj) {
        if (simulationObj == null) {
            throw new IllegalArgumentException("Missing 'simulation' key in configuration.");
        }
        if (!(simulationObj instanceof Map)) {
            throw new IllegalArgumentException("'simulation' is not a Map. Found type: " + simulationObj.getClass().getName());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> simulationMap = (Map<String, Object>) simulationObj;

        Object agentsObj = simulationMap.get("agents");
        if (agentsObj == null) {
            throw new IllegalArgumentException("Missing 'agents' key in simulation configuration.");
        }
        if (!(agentsObj instanceof List)) {
            throw new IllegalArgumentException("'agents' is not a List. Found type: " + agentsObj.getClass().getName());
        }
        @SuppressWarnings("unchecked")
        List<Object> rawAgentsList = (List<Object>) agentsObj;
        return rawAgentsList;
    }
}
