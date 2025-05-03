package com.energytwin.microgrid.dto;

import lombok.Getter;

@Getter
public final class EnergySource implements AgentType {
    private String name;
    private  float productionRate;

    @Override
    public String getType() {
        return "energySource";
    }
}
