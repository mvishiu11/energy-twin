package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractLoadAgent;
import com.energytwin.microgrid.core.behaviours.tick.TickSubscriberBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.Map;

/** Load Agent that consumes energy and broadcasts its consumption. */
public class LoadAgent extends AbstractLoadAgent {

  @Override
  protected void onAgentSetup() {
    setConfigParams();

    AID tickTopic = new AID("TICK_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new TickSubscriberBehaviour(this, tickTopic));
  }

  @Override
  protected void setConfigParams() {
    String type = "load";
    String param = "consumptionRate";
    Map<String, Object> myConfig =
        simulationConfigService.findAgentDefinition(type, getLocalName());
    if (myConfig != null) {
      Object rateObj = myConfig.get(param);
      if (rateObj != null) {
        try {
          this.consumptionRate = Double.parseDouble(rateObj.toString());
        } catch (NumberFormatException e) {
          log("Error parsing " + param + ": " + rateObj, e);
        }
      }
    } else {
      log("No configuration found for agent of type " + type + " with name: " + getLocalName());
    }
    log("Load Agent started with " + param + ": " + consumptionRate);
  }

  @Override
  public void onTick(long simulationTime) {
    double consumed = consumptionRate;
    log("Consumed energy: " + consumed + " kW at simulation time: " + simulationTime);

    reportState(consumed, 0.0, 0.0);

    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setOntology("ENERGY_CONSUMPTION");
    msg.setContent(String.valueOf(consumed));
    msg.addReceiver(new jade.core.AID("AggregatorAgent", jade.core.AID.ISLOCALNAME));
    send(msg);
  }
}
