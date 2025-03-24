package com.energytwin.microgrid.controller;

import com.energytwin.microgrid.service.JadeContainerService;
import com.energytwin.microgrid.service.LogAggregatorService;
import com.energytwin.microgrid.service.SimulationControlService;
import com.energytwin.microgrid.service.SimulationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for simulation operations.
 */
@RestController
@RequestMapping("/simulate")
public class SimulationController {

    @Autowired
    private JadeContainerService jadeContainerService;

    @Autowired
    private LogAggregatorService logService;

    @Autowired
    private SimulationControlService simulationControlService;

    @Autowired
    private SimulationConfigService simulationConfigService;

    private final String AGENT_BASE_PATH = "com.energytwin.microgrid.core.agents.";

    /**
     * Starts the simulation by launching agents according to the configuration.
     */
    @PostMapping("/start")
    public ResponseEntity<String> startSimulation() {
        jadeContainerService.startContainer();
        jadeContainerService.launchAgent("OrchestratorAgent", AGENT_BASE_PATH + "OrchestratorAgent");
        jadeContainerService.launchAgent("AggregatorAgent", AGENT_BASE_PATH + "AggregatorAgent");

        // Load agent configuration from JSON file
        Object agentsObj = ((java.util.Map<?, ?>) simulationConfigService.getConfig().get("simulation")).get("agents");
        if (agentsObj instanceof List<?> agentsList) {
            for (Object agentDef : agentsList) {
                if (agentDef instanceof java.util.Map) {
                    java.util.Map<String, Object> agentConfig = (java.util.Map<String, Object>) agentDef;
                    String type = (String) agentConfig.get("type");
                    String name = (String) agentConfig.get("name");
                    // Decide agent class based on type
                    String className;
                    switch (type) {
                        case "energySource":
                            className = AGENT_BASE_PATH + "EnergySourceAgent";
                            break;
                        case "energyStorage":
                            className = AGENT_BASE_PATH + "EnergyStorageAgent";
                            break;
                        case "load":
                            className = AGENT_BASE_PATH + "LoadAgent";
                            break;
                        default:
                            continue; // Unknown type, skip
                    }
                    jadeContainerService.launchAgent(name, className);
                }
            }
        }

        return ResponseEntity.ok("Simulation started.");
    }

    @GetMapping("/logs")
    public ResponseEntity<List<String>> getLogs() {
        return ResponseEntity.ok(logService.getLogs());
    }

    @PostMapping("/control/speed")
    public ResponseEntity<String> setSpeed(@RequestParam("factor") int factor) {
        simulationControlService.setSpeedUpFactor(factor);
        return ResponseEntity.ok("Speed factor set to " + factor);
    }

    @PostMapping("/control/pause")
    public ResponseEntity<String> pauseSimulation() {
        simulationControlService.pause();
        return ResponseEntity.ok("Simulation paused.");
    }

    @PostMapping("/control/resume")
    public ResponseEntity<String> resumeSimulation() {
        simulationControlService.resume();
        return ResponseEntity.ok("Simulation resumed.");
    }
}
