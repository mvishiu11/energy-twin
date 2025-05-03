package com.energytwin.microgrid.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SimulationConfig {
    private float tickIntervalMillis;
    private float externalSourceCost;
    private float externalSourceCap;
    private List<AgentType> agents;
}
