package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractSimAgent;
import com.energytwin.microgrid.core.behaviours.TickSubscriberBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

/** Load Agent that consumes energy and broadcasts its consumption. */
public class LoadAgent extends AbstractSimAgent {

  private double consumptionRate;

  @Override
  protected void onAgentSetup() {
    initSpring();
    this.consumptionRate = 30.0;
    log("Load Agent started with consumption rate: " + consumptionRate);

    AID tickTopic = new AID("TickTopic", AID.ISLOCALNAME);
    addBehaviour(new TickSubscriberBehaviour(this, tickTopic));
  }

  @Override
  public void onTick(long simulationTime) {
    double consumed = consumptionRate;
    log("Consumed energy: " + consumed + " kW at simulation time: " + simulationTime);

    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setOntology("ENERGY_CONSUMPTION");
    msg.setContent(String.valueOf(consumed));
    msg.addReceiver(new jade.core.AID("AggregationTopic", jade.core.AID.ISLOCALNAME));
    send(msg);
  }
}
