package com.energytwin.microgrid.core.behaviours.energy;

import com.energytwin.microgrid.core.base.AbstractEnergyStorageAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/** A unified class to handle Contract-Net Protocol messages for shortfall/surplus. */
public class BatteryCNPResponder extends CyclicBehaviour {
  private final AbstractEnergyStorageAgent agent;
  private static final String ONT_CFP_SHORTFALL = "CNP_SHORTFALL";
  private static final String ONT_CFP_SURPLUS = "CNP_SURPLUS";
  private static final String ONT_ACCEPT = "CNP_ACCEPT";
  private static final String ONT_REJECT = "CNP_REJECT";

  public BatteryCNPResponder(
      AbstractEnergyStorageAgent agent, AID shortfallTopic, AID surplusTopic) {
    super(agent);
    this.agent = agent;
    try {
      TopicManagementHelper topicHelper =
          (TopicManagementHelper) agent.getHelper(TopicManagementHelper.SERVICE_NAME);
      topicHelper.register(shortfallTopic);
      topicHelper.register(surplusTopic);
      agent.log(
          "Successfully subscribed to CFP topics: "
              + shortfallTopic.getLocalName()
              + " and "
              + surplusTopic.getLocalName());
    } catch (Exception e) {
      agent.log("Error subscribing to CFP topics: {}", e.getMessage(), e);
    }
  }

  @Override
  public void action() {
    ACLMessage msg =
        myAgent.receive(
            MessageTemplate.or(
                MessageTemplate.MatchOntology(ONT_CFP_SHORTFALL),
                MessageTemplate.MatchOntology(ONT_CFP_SURPLUS)));
    if (msg != null) {
      handleCFP(msg);
    } else {
      ACLMessage decision =
          myAgent.receive(
              MessageTemplate.or(
                  MessageTemplate.MatchOntology(ONT_ACCEPT),
                  MessageTemplate.MatchOntology(ONT_REJECT)));
      if (decision != null) {
        handleDecision(decision);
      } else {
        block();
      }
    }
  }

  private void handleCFP(ACLMessage msg) {
    String ontology = msg.getOntology();
    double requested = Double.parseDouble(msg.getContent());
    ACLMessage proposal = msg.createReply();
    proposal.setPerformative(ACLMessage.PROPOSE);
    proposal.setOntology("CNP_PROPOSAL");
    // Make sure aggregator knows which CFP type we are replying to
    proposal.setInReplyTo(ontology);

    if (ONT_CFP_SHORTFALL.equals(ontology)) {
      // aggregator needs X kW => we propose supply = min(X, currentSoC)
      double canSupply = Math.min(requested, agent.getAvailableToDischarge());
      if (canSupply > 0.0001) {
        // respond with "supply=...,cost=..."
        proposal.setContent("supply=" + canSupply + ";cost=" + agent.getCost());
      } else {
        // If we can't supply anything, we might not respond or we respond with 0
        // Let’s respond with 0 supply so aggregator can ignore us
        proposal.setContent("supply=0;cost=9999");
      }
      agent.log("Proposed: " + proposal.getContent());
      agent.send(proposal);

    } else if (ONT_CFP_SURPLUS.equals(ontology)) {
      // aggregator has X surplus => we propose store = min(X, capacity - currentSoC)
      double canStore = Math.min(requested, agent.getAvailableToCharge());
      if (canStore > 0.0001) {
        proposal.setContent("store=" + canStore + ";cost=" + agent.getCost());
      } else {
        // respond with 0
        proposal.setContent("store=0;cost=9999");
      }
      agent.log("Proposed: " + proposal.getContent());
      agent.send(proposal);
    }
  }

  private void handleDecision(ACLMessage msg) {
    String ontology = msg.getOntology();
    String content = msg.getContent();
    if (ONT_ACCEPT.equals(ontology)) {
      // parse "acceptedAmount=XX"
      // apply to SoC
      double acceptedAmount = parseAcceptedAmount(content);
      if (msg.getInReplyTo() != null) {
        if (msg.getInReplyTo().equals(ONT_CFP_SHORTFALL)) {
          // We are discharging
          agent.currentSoC -= acceptedAmount;
          if (agent.currentSoC < 0) {
            agent.currentSoC = 0;
          }
          agent.log("Discharged " + acceptedAmount + " kW. New SoC=" + agent.currentSoC);
        } else if (msg.getInReplyTo().equals(ONT_CFP_SURPLUS)) {
          // We are charging
          agent.currentSoC += acceptedAmount;
          if (agent.currentSoC > agent.getCapacity()) {
            agent.currentSoC = agent.getCapacity();
          }
          agent.log("Charged " + acceptedAmount + " kW. New SoC=" + agent.currentSoC);
        }
      }
    } else if (ONT_REJECT.equals(ontology)) {
      // aggregator did not accept our proposal
      agent.log("Proposal rejected: " + content);
    }
  }

  private double parseAcceptedAmount(String content) {
    double amt = 0;
    // example content "acceptedAmount=5.0"
    String[] tokens = content.split("=");
    if (tokens.length == 2) {
      try {
        amt = Double.parseDouble(tokens[1]);
      } catch (NumberFormatException e) {
        agent.log("Failed to parse accepted amount from: " + content, e);
      }
    }
    return amt;
  }
}
