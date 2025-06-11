package com.energytwin.microgrid.core.planner;

/** A single command executed at tickOffset (0 = “now”). */
public record Action(int tickOffset,
                     String target,
                     double chargeKw,
                     double extImportKw) {
}
