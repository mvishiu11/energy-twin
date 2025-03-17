package com.energytwin.microgrid.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.InputStream;
import java.util.Map;

/**
 * Loads simulation configuration from an external JSON file.
 */
@Service
public class SimulationConfigService {

    @Value("classpath:simulation-config.json")
    private Resource configResource;

    @Getter
    private Map<String, Object> config;

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
     * Helper method to retrieve a global simulation parameter.
     * @param key the parameter key.
     * @return value as Object.
     */
    public Object getGlobalParameter(String key) {
        Map<String, Object> simConfig = (Map<String, Object>) config.get("simulation");
        return simConfig.get(key);
    }
}
