package com.energytwin.microgrid.ws.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetricsMessage {
    private long tickNumber;
    private double totalProduced;
    private double totalConsumed;
    private double cnpNegotiations;
    private double totalProducedPerNTicks;
    private double totalDemandPerNTicks;
    private double greenEnergyRatioPct;

    private double rmseLoadKw;
    private double rmsePvKw;
    private double[] forecastLoadKw;
    private double[] forecastPvKw;
}
