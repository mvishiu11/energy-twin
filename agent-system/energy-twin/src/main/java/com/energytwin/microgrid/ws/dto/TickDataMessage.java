package com.energytwin.microgrid.ws.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TickDataMessage {
    private long tickNumber;
    private Map<String, AgentState> agentStates;

    /* --- aggregate forecast & error --- */
    private double predictedLoadKw;
    private double predictedPvKw;
    private double errorLoadKw;
    private double errorPvKw;

    @Getter
    @Setter
    public static class AgentState {

        private double demand;

        private double production;

        private double stateOfCharge;

        private boolean broken;

    }
}
