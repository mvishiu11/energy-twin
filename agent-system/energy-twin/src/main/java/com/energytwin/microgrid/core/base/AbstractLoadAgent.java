package com.energytwin.microgrid.core.base;

import lombok.Setter;

/**
 * Abstract base class for load agents. Includes common properties and methods for agents that
 * consume energy.
 */
@Setter
public abstract class AbstractLoadAgent extends AbstractSimAgent {
  protected double consumptionRate;
}
