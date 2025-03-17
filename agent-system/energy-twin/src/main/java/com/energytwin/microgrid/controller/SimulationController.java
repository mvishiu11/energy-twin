package com.energytwin.microgrid.controller;

import com.energytwin.microgrid.service.JadeContainerService;
import com.energytwin.microgrid.service.LogAggregatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/simulate")
public class SimulationController {

    @Autowired
    private JadeContainerService jadeContainerService;

    @Autowired
    private LogAggregatorService logService;

    @PostMapping("/start")
    public ResponseEntity<String> startSimulation() throws ExecutionException, InterruptedException {
        // Initialize JADE container if necessary
        jadeContainerService.startContainer();
        // Launch the Energy Source Agent (and other agents as needed)
        jadeContainerService.launchAgent("EnergyAgent", "com.energytwin.microgrid.core.agents.EnergySourceAgent");
        return ResponseEntity.ok("Simulation started.");
    }

    @GetMapping("/logs")
    public ResponseEntity<List<String>> getLogs() {
        return ResponseEntity.ok(logService.getLogs());
    }
}
