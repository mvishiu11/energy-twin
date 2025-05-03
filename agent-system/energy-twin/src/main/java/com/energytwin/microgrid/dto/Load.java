package com.energytwin.microgrid.dto;

import lombok.Getter;

@Getter
public final class Load implements AgentType {
    private String name;
    private float consumptionRate;

    @Override
    public String getType() {
        return "load";
    }
}
