package com.energytwin.microgrid.core.scenario;

import java.util.List;

/** Converts probabilistic forecasts into a set of discrete scenarios. */
public interface ScenarioGenerator {
    List<Scenario> generate(double[][] loadQ, double[][] pvQ);
}