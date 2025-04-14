package com.energytwin.microgrid.core.behaviours.aggregator;

import com.energytwin.microgrid.core.agents.AggregatorAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ProductionConsumptionListener extends CyclicBehaviour {

  AggregatorAgent agent;

  public ProductionConsumptionListener(AggregatorAgent agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {
    MessageTemplate matchTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    ACLMessage msg = myAgent.receive(matchTemplate);
    if (msg != null) {
      String ontology = msg.getOntology();
      if (AggregatorAgent.ONT_PRODUCTION.equals(ontology)) {
        double produced = Double.parseDouble(msg.getContent());
        agent.totalProductionThisTick += produced;
        agent.productionMap.put(msg.getSender().getLocalName(), produced);
      } else if (AggregatorAgent.ONT_CONSUMPTION.equals(ontology)) {
        double consumed = Double.parseDouble(msg.getContent());
        agent.totalConsumptionThisTick += consumed;
        agent.consumptionMap.put(msg.getSender().getLocalName(), consumed);
      }
    } else {
      block();
    }
  }
}
