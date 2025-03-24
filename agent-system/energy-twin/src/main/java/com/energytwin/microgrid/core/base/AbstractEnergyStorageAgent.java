package com.energytwin.microgrid.core.base;

import com.energytwin.microgrid.agentfusion.SpringAgent;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for energy storage agents. Includes common properties and methods for agents
 * that store energy.
 */
@Getter
@Setter
public abstract class AbstractEnergyStorageAgent extends SpringAgent {
  protected double capacity;
  protected double currentStored = 0.0;
}
