package com.energytwin.microgrid.ws.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetricsMessage {
    private long tickNumber;
    private double totalProduced;
    private double totalConsumed;
    private int cnpNegotiations;

    private double greenEnergyRatioPct;
}
