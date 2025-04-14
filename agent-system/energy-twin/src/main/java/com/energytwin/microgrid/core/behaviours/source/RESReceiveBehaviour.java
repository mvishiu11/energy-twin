package com.energytwin.microgrid.core.behaviours.source;

import com.energytwin.microgrid.core.base.AbstractEnergySourceAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RESReceiveBehaviour extends CyclicBehaviour {

  private final AbstractEnergySourceAgent agent;

  public RESReceiveBehaviour(AbstractEnergySourceAgent agent, AID irradianceTopic) {
    super(agent);
    this.agent = agent;

    try {
      TopicManagementHelper helper =
          (TopicManagementHelper) agent.getHelper(TopicManagementHelper.SERVICE_NAME);
      helper.register(irradianceTopic);
    } catch (Exception e) {
      agent.log("Failed to register IRRADIANCE_TOPIC", e);
    }
  }

  @Override
  public void action() {
    ACLMessage msg = myAgent.receive(MessageTemplate.MatchOntology("IRRADIANCE"));
    if (msg != null) {
      try {
        agent.setLatestIrradiance(Double.parseDouble(msg.getContent()));
        agent.log("Received irradiance: " + agent.getLatestIrradiance() + " W/m²");
      } catch (NumberFormatException e) {
        agent.log("Malformed irradiance message: " + msg.getContent());
      }
    } else {
      block();
    }
  }
}
