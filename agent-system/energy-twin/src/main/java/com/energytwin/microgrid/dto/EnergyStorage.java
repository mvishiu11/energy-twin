package com.energytwin.microgrid.dto;

import lombok.Getter;

@Getter
public final class EnergyStorage implements AgentType {
    private String name;
    private float capacity;
    private float cost;
    private float initialSoC;

    @Override
    public String getType() {
        return "energyStorage";
    }
}
