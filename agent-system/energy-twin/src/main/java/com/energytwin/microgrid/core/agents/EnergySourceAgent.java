package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractEnergySourceAgent;
import com.energytwin.microgrid.core.behaviours.source.RESReceiveBehaviour;
import com.energytwin.microgrid.core.behaviours.tick.TickSubscriberBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.Map;

/** Energy Source Agent that produces energy at each tick and broadcasts production. */
public class EnergySourceAgent extends AbstractEnergySourceAgent {

  @Override
  protected void onAgentSetup() {
    setConfigParams();

    AID tickTopic = new AID("TICK_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new TickSubscriberBehaviour(this, tickTopic));

    AID irradianceTopic = new AID("IRRADIANCE_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new RESReceiveBehaviour(this, irradianceTopic));
  }

  @Override
  protected void setConfigParams() {
    String type = "energySource";
    Map<String, Object> config = simulationConfigService.findAgentDefinition(type, getLocalName());
    if (config != null) {
      this.efficiency = parse(config, "efficiency", 0.2);
      this.area = parse(config, "area", 1.6);
    } else {
      log("No config found for " + getLocalName());
    }
    log("Solar panel: efficiency=" + efficiency + ", area=" + area);
  }

  private double parse(Map<String, Object> config, String key, double fallback) {
    Object value = config.get(key);
    if (value != null) {
      try {
        return Double.parseDouble(value.toString());
      } catch (Exception e) {
        log("Error parsing " + key + ": " + value);
      }
    }
    return fallback;
  }

  @Override
  public void onTick(long simulationTime) {
    double powerKW = latestIrradiance * area * efficiency;
    log("Produced: " + powerKW + " kW (Irradiance=" + latestIrradiance + ")");

    reportState(0.0, powerKW, 0.0);

    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setOntology("ENERGY_PRODUCTION");
    msg.setContent(String.valueOf(powerKW));
    msg.addReceiver(new AID("AggregatorAgent", AID.ISLOCALNAME));
    send(msg);
  }
}
