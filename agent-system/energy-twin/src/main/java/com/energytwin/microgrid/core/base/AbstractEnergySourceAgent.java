package com.energytwin.microgrid.core.base;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for energy source agents. Includes common properties and methods for agents
 * that produce energy.
 */
@Setter
public abstract class AbstractEnergySourceAgent extends AbstractSimAgent {
  protected double efficiency;
  protected double area;
  @Getter protected double latestIrradiance;

  protected abstract void setConfigParams();
}
