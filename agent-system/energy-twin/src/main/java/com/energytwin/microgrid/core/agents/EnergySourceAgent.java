package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractEnergySourceAgent;
import com.energytwin.microgrid.core.behaviours.source.RESReceiveBehaviour;
import com.energytwin.microgrid.core.behaviours.tick.TickSubscriberBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Map;

/** Physically-based PV panel agent (NOCT + temperature coefficient model). */
public final class EnergySourceAgent extends AbstractEnergySourceAgent {


  private static final double INV_EFF = 0.96;   // inverter efficiency

  @Override
  protected void onAgentSetup() {
    setConfigParams();

    // subscribe to tick
    AID tickTopic = new AID("TICK_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new TickSubscriberBehaviour(this, tickTopic));

    // subscribe to irradiance
    AID irrTopic = new AID("IRRADIANCE_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new RESReceiveBehaviour(this, irrTopic));
  }

  @Override
  protected void setConfigParams() {
    Map<String, Object> cfg =
            simulationConfigService.findAgentDefinition("energySource", getLocalName());

    noOfPanels = (int) get(cfg, "noOfPanels", 500);
    efficiency25 = get(cfg, "efficiency", 0.20);
    areaM2       = get(cfg, "area",       1.6);
    tempCoeff    = get(cfg, "tempCoeff", -0.0038);  // −0.38 % / °C
    noct         = get(cfg, "noct",       45.0);

    log("PV parameters: η25=%.3f  γ=%.4f  A=%.2f m²  NOCT=%.1f °C"
            .formatted(efficiency25, tempCoeff, areaM2, noct));
  }

  private double get(Map<String, Object> cfg, String k, double def) {
    if (cfg == null) return def;
    Object v = cfg.get(k);
    return v == null ? def : Double.parseDouble(v.toString());
  }

  /* ---------- main tick ---------- */

  @Override
  public void onTick(long t) {
    boolean isBroken = eventControlService.isBroken(getLocalName());
    double PkW = computePVPowerKW(latestIrradiance, ambientTemp);
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setOntology("ENERGY_PRODUCTION");
    if( isBroken){
      msg.setContent(String.valueOf(0));
    }else{
      msg.setContent(String.valueOf(PkW));
    }
    msg.addReceiver(new AID("AggregatorAgent", AID.ISLOCALNAME));
    send(msg);

    reportState(0.0, isBroken ? 0 : PkW, 0.0, isBroken ? true : false);

    log("t=%d  G=%.1f W/m²  Ta=%.1f °C  P=%.2f kW".formatted(t, latestIrradiance, ambientTemp, PkW));
  }

  /* ---------- physics ---------- */

  private double computePVPowerKW(double G, double Ta) {
    if (G <= 0) return 0.0;

    // Cell temperature via NOCT model
    double Tc = Ta + (G / 800.0) * (noct - 20.0);

    // Temperature-corrected efficiency
    double eta = efficiency25 * (1.0 + tempCoeff * (Tc - 25.0));

    // DC power
    double Pdc = G * areaM2 * eta;     // W

    // AC power after inverter
    return Math.max(0.0, Pdc * INV_EFF / 1000.0) * noOfPanels; // kW
  }
}
