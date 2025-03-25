package com.energytwin.microgrid.controller;

import com.energytwin.microgrid.service.JadeContainerService;
import com.energytwin.microgrid.service.LogAggregatorService;
import com.energytwin.microgrid.service.SimulationConfigService;
import com.energytwin.microgrid.service.SimulationControlService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for simulation operations. */
@RestController
@RequestMapping("/simulation")
public class SimulationController {

  @Autowired
  private JadeContainerService jadeContainerService;

  @Autowired
  private LogAggregatorService logService;

  @Autowired
  private SimulationControlService simulationControlService;

  @Autowired
  private SimulationConfigService simulationConfigService;

    /**
   * Starts the simulation by receiving a startup configuration (JSON) via the request body,
   * updating the SimulationConfigService with it, and then launching agents accordingly.
   *
   * @param configJson the simulation configuration as a JSON string
   * @return HTTP response indicating success or error
   */
  @PostMapping("/start")
  public ResponseEntity<String> startSimulation(@RequestBody String configJson) {
    try {
      // Update the simulation configuration with the provided JSON
      simulationConfigService.setConfigFromString(configJson);

      // Start the JADE container and launch core agents
      jadeContainerService.startContainer();
        String AGENT_BASE_PATH = "com.energytwin.microgrid.core.agents.";
        jadeContainerService.launchAgent("OrchestratorAgent", AGENT_BASE_PATH + "OrchestratorAgent");
      jadeContainerService.launchAgent("AggregatorAgent", AGENT_BASE_PATH + "AggregatorAgent");

      // Load agent configuration from the updated configuration
      Object agentsObj =
              ((Map<?, ?>) simulationConfigService.getConfig().get("simulation")).get("agents");
      if (agentsObj instanceof List<?> agentsList) {
        for (Object agentDef : agentsList) {
          if (agentDef instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> agentConfig = (Map<String, Object>) agentDef;
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
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Error starting simulation: " + e.getMessage());
    }
  }

  @PostMapping("/stop")
  public ResponseEntity<String> stopSimulation() {
    try {
      jadeContainerService.stopContainer();
      return ResponseEntity.ok("Simulation stopped.");
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Error stopping simulation: " + e.getMessage());
    }
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
