package com.energytwin.microgrid.core.behaviours.energy;

import com.energytwin.microgrid.core.base.AbstractEnergyStorageAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class EnergyStorageBehaviour extends CyclicBehaviour {

  private final AbstractEnergyStorageAgent agent;

  public EnergyStorageBehaviour(AbstractEnergyStorageAgent agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {
    MessageTemplate mt = MessageTemplate.MatchOntology("ENERGY_ALLOCATION");
    ACLMessage msg = myAgent.receive(mt);
    if (msg != null) {
      try {
        double allocated = Double.parseDouble(msg.getContent());
        this.agent.log("Received allocation: " + allocated);
        this.agent.setCurrentStored(
            Math.min(this.agent.getCurrentStored() + allocated, this.agent.getCapacity()));
        this.agent.log("Current stored energy: " + this.agent.getCurrentStored());
      } catch (NumberFormatException e) {
        this.agent.log("Error parsing allocation message: " + msg.getContent());
      }
    } else {
      block();
    }
  }
}
