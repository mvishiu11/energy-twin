package com.energytwin.microgrid.core.base;

import lombok.Getter;
import lombok.Setter;

/** Physical battery model shared by all storage agents. */
@Setter
@Getter
public abstract class AbstractEnergyStorageAgent extends AbstractSimAgent {

  /* Config parameters (kWh, kW, efficiencies) */
  protected double capacityKwh;         // energy capacity
  protected double chargeEffBase;       // η_c (e.g. 0.94)
  protected double dischargeEffBase;    // η_d (e.g. 0.92)
  protected double cRate;               // h⁻¹  (0.5 => 2-hour battery)
  protected double selfDischargePerHour;// fraction (e.g. 3.9e-4)

  /* State */
  protected double socKwh;              // current state-of-charge

  /* -------- derived helpers -------- */

  /** Pmax (kW) allowed per tick (charge or discharge). */
  public double powerCapKw() { return cRate * capacityKwh; }

  /** Available discharge energy (kWh) this tick, with derated η_d. */
  public double getAvailableToDischarge() {
    double ηd = dischargeEffEff();
    return socKwh * ηd;
  }

  /** Available charge headroom (kWh) this tick, limited by Pmax. */
  public double getAvailableToCharge() {
    double ηc = chargeEffEff();
    return Math.min(powerCapKw(), (capacityKwh - socKwh) / ηc);
  }

  /** Linear-derated discharge efficiency. */
  public double dischargeEffEff() {
    if (socKwh < 0.10 * capacityKwh)
      return dischargeEffBase * (socKwh / (0.10 * capacityKwh));
    return dischargeEffBase;
  }

  /** Linear-derated charge efficiency. */
  public double chargeEffEff() {
    if (socKwh > 0.90 * capacityKwh)
      return chargeEffBase * ((capacityKwh - socKwh) / (0.10 * capacityKwh));
    return chargeEffBase;
  }

  /** Should be called each tick for self-discharge. */
  public void applySelfDischarge() {
    socKwh *= (1.0 - selfDischargePerHour);
  }

  protected abstract void setConfigParams();
}
