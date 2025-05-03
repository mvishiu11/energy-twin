package com.energytwin.microgrid.dto;

sealed public interface AgentType permits EnergySource, EnergyStorage, Load {
    String getType();
}
