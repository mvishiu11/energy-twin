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
    private double predictedLoadKw;   // median (q50)
    private double predictedPvKw;     // median (q50)
    private double errorLoadKw;       // RMSE-increment
    private double errorPvKw;
    private double[] fanLoLoad;           // length = H_pred : q05
    private double[] fanHiLoad;           // length = H_pred : q95
    private double[] fanLoPv;
    private double[] fanHiPv;

    @Getter
    @Setter
    public static class AgentState {

        private double cnpNegotiations;

        private double demand;

        private double production;

        private double stateOfCharge;

        private boolean broken;

    }
}
