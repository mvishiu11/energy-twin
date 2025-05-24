package com.energytwin.microgrid.core.base;

import lombok.Getter;
import lombok.Setter;

@Setter
public abstract class AbstractEnergySourceAgent extends AbstractSimAgent {
  protected int    noOfPanels;
  protected double efficiency25;     // η at 25 °C
  protected double areaM2;           // panel area
  protected double tempCoeff;        // γ (%/°C, negative)
  protected double noct;             // °C
  @Getter
  protected double latestIrradiance;
  @Setter
  @Getter protected double ambientTemp = 25.0;

    protected abstract void setConfigParams();
}
