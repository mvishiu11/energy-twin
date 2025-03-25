package com.energytwin.microgrid.core.base;

import lombok.Setter;

/**
 * Abstract base class for energy source agents. Includes common properties and methods for agents
 * that produce energy.
 */
@Setter
public abstract class AbstractEnergySourceAgent extends AbstractSimAgent {
  /** -- SETTER -- Sets the production rate for this energy source. */
  protected double productionRate;
}
