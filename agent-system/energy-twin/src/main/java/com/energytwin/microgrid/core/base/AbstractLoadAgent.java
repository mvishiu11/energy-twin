package com.energytwin.microgrid.core.base;

import lombok.Getter;
import lombok.Setter;

/** Common base for building / load agents that consume energy. */
@Setter
@Getter
public abstract class AbstractLoadAgent extends AbstractSimAgent {

  /** Nominal design-load of the building (kW at 100 %). */
  protected double nominalLoadKw;

  /** Implementations must populate nominalLoadKw from config. */
  protected abstract void setConfigParams();
}
