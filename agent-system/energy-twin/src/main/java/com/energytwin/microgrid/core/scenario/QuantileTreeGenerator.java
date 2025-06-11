package com.energytwin.microgrid.core.scenario;

import java.util.ArrayList;
import java.util.List;

/** 3-branch quantile tree: low (q05), median (q50), high (q95). */
public final class QuantileTreeGenerator implements ScenarioGenerator {

    private final int H;

    public QuantileTreeGenerator(int horizon){ this.H = horizon; }

    @Override public List<Scenario> generate(double[][] loadQ, double[][] pvQ) {

        double[] loL = loadQ[0], medL = loadQ[1], hiL = loadQ[2];
        double[] loP = pvQ  [0], medP = pvQ  [1], hiP = pvQ  [2];

        List<Scenario> out = new ArrayList<>(3);
        out.add(new Scenario(loL, loP, 0.15));   // pessimistic
        out.add(new Scenario(medL, medP,0.70));  // median
        out.add(new Scenario(hiL, hiP, 0.15));   // optimistic
        return out;
    }
}
