package com.energytwin.microgrid.core.base;

import com.energytwin.microgrid.agentfusion.SpringAgent;

/**
 * Abstract base class for all simulation agents. Defines a common tick behavior that all simulation
 * agents must implement.
 */
public abstract class AbstractSimAgent extends SpringAgent {

  /**
   * Called on every simulation tick.
   *
   * @param simulationTime the current simulation time
   */
  public abstract void onTick(long simulationTime);
}
