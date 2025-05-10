package com.energytwin.microgrid.ws.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TickDataMessage {
    private long tickNumber;
    private Map<String, AgentState> agentStates;

    @Getter
    @Setter
    public static class AgentState {

        private double demand;

        private double production;

        private double stateOfCharge;

    }
}
