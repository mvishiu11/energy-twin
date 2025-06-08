package com.energytwin.microgrid.core.scenario;

/** One realisation of the next H_pred hours. */
public record Scenario(double[] loadKw, double[] pvKw, double probability) { }

