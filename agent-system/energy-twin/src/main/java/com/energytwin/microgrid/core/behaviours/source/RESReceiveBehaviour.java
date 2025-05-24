package com.energytwin.microgrid.core.behaviours.source;

import com.energytwin.microgrid.core.base.AbstractEnergySourceAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/** Listens for IRRADIANCE messages and updates `latestIrradiance` + ambient temperature. */
public final class RESReceiveBehaviour extends CyclicBehaviour {

  private final AbstractEnergySourceAgent pv;

  public RESReceiveBehaviour(AbstractEnergySourceAgent pv, AID irradianceTopic) {
    super(pv);
    this.pv = pv;
    // auto-subscribe
    try {
      ((jade.core.messaging.TopicManagementHelper)
              pv.getHelper(jade.core.messaging.TopicManagementHelper.SERVICE_NAME))
              .register(irradianceTopic);
    } catch (Exception e) {
      pv.log("Could not register IRRADIANCE_TOPIC: {}", e.getMessage(), e);
    }
  }

  @Override
  public void action() {
    ACLMessage m = myAgent.receive(MessageTemplate.MatchOntology("IRRADIANCE"));
    if (m == null) { block(); return; }

    String[] tokens = m.getContent().split(";");
    double G = 0, Ta = 25;
    for (String t : tokens) {
      String[] kv = t.split("=");
      if (kv.length == 2) {
        if ("G".equals(kv[0])) G  = Double.parseDouble(kv[1]);
        if ("T".equals(kv[0])) Ta = Double.parseDouble(kv[1]);
      }
    }
    pv.setLatestIrradiance(G);
    pv.setAmbientTemp(Ta);
  }
}
