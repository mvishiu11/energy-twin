package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractEnergyStorageAgent;
import com.energytwin.microgrid.core.behaviours.energy.BatteryCNPResponder;
import com.energytwin.microgrid.core.behaviours.tick.TickSubscriberBehaviour;
import jade.core.AID;
import lombok.Setter;

import java.util.Map;

public final class EnergyStorageAgent extends AbstractEnergyStorageAgent {

  private double cnpNegotiations = 0;
  @Setter
  private double dsoc = 0;

  @Override
  protected void onAgentSetup() {
    setConfigParams();

    AID shortfallTopic = new AID("CNP_SHORTFALL_TOPIC", AID.ISLOCALNAME);
    AID surplusTopic   = new AID("CNP_SURPLUS_TOPIC",   AID.ISLOCALNAME);
    AID tickTopic      = new AID("TICK_TOPIC",          AID.ISLOCALNAME);

    addBehaviour(new BatteryCNPResponder(this, shortfallTopic, surplusTopic));
    addBehaviour(new TickSubscriberBehaviour(this, tickTopic));

    log("Battery %.0f kWh  ηc=%.2f  ηd=%.2f  C-rate=%.2f h⁻¹  self-d=%.2e  SoC=%.1f",
            capacityKwh, chargeEffBase, dischargeEffBase, cRate, selfDischargePerHour, socKwh);
  }

  @Override
  protected void setConfigParams() {
    Map<String, Object> cfg =
            simulationConfigService.findAgentDefinition("energyStorage", getLocalName());
    if (cfg == null)
      throw new IllegalArgumentException("No config for battery " + getLocalName());

    capacityKwh         = dbl(cfg, "capacity",      300.0);
    chargeEffBase       = dbl(cfg, "etaCharge",     0.94);
    dischargeEffBase    = dbl(cfg, "etaDischarge",  0.92);
    cRate               = dbl(cfg, "cRate",         0.5);
    selfDischargePerHour= dbl(cfg, "selfDischarge", 3.9e-4);
    socKwh              = dbl(cfg, "initialSoC",    0.10 * capacityKwh);
  }

  private double dbl(Map<String, Object> m, String k, double def) {
    Object v = m.get(k); return v == null ? def : Double.parseDouble(v.toString());
  }

  /* tick bookkeeping */
  @Override public void onTick(long t) {

    if (eventControlService.isBroken(getLocalName())) {
      log("t=%d  AGENT BROKEN – skipping tick", t);
      reportState(0,0,-1, true, cnpNegotiations);
      return;
    }

    applySelfDischarge();
    if( dsoc > 0 ){
      reportState(0,dsoc ,socKwh, false, cnpNegotiations);
    }else{
      reportState(dsoc,0 ,socKwh, false, cnpNegotiations);
    }
    log("t=%d  SoC=%.2f kWh (%.0f %%)".formatted(t, socKwh, 100*socKwh/capacityKwh));

  }

  public void incrementCnpNegotiations() {
    cnpNegotiations++;
  }

}
