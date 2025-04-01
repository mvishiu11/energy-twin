package com.energytwin.microgrid.core.base;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for energy storage agents. Includes common properties and methods for agents
 * that store energy.
 */
@Getter
@Setter
public abstract class AbstractEnergyStorageAgent extends AbstractSimAgent {
  protected double capacity; // Maximum capacity in kW
  public double currentSoC; // Current State of Charge
  protected double cost; // Cost metric (single or separate for charge/discharge)

  public double getAvailableToDischarge() {
    return currentSoC;
  }

  public double getAvailableToCharge() {
    return capacity - currentSoC;
  }

  protected abstract void setConfigParams();
}
