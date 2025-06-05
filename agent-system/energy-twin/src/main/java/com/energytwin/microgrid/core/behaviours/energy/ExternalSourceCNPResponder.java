package com.energytwin.microgrid.core.behaviours.energy;

import com.energytwin.microgrid.core.agents.ExternalEnergySourceAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ExternalSourceCNPResponder extends CyclicBehaviour {
  private final ExternalEnergySourceAgent agent;
  private static final String ONT_CFP_SHORTFALL = "CNP_SHORTFALL";
  private static final String ONT_PROPOSAL = "CNP_PROPOSAL";
  private static final String ONT_ACCEPT = "CNP_ACCEPT";
  private static final String ONT_REJECT = "CNP_REJECT";

  public ExternalSourceCNPResponder(ExternalEnergySourceAgent agent, AID shortfallTopic) {
    super(agent);
    this.agent = agent;
    try {
      TopicManagementHelper topicHelper =
          (TopicManagementHelper) agent.getHelper(TopicManagementHelper.SERVICE_NAME);
      topicHelper.register(shortfallTopic);
      agent.log("Successfully subscribed to CFP topics: " + shortfallTopic.getLocalName());
    } catch (Exception e) {
      agent.log("Error subscribing to CFP topics: {}", e.getMessage(), e);
    }
  }

  @Override
  public void action() {
    ACLMessage msg = myAgent.receive(MessageTemplate.MatchOntology(ONT_CFP_SHORTFALL));
    if (msg != null) {
      handleShortfallCFP(msg);
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

  private void handleShortfallCFP(ACLMessage cfp) {
    double requested = Double.parseDouble(cfp.getContent());
    double canSupply = Math.min(requested, agent.maxSupplyPerTick);

    if (agent.getEventControlService().getBlackoutRemaining() > 0) {
      canSupply = 0.0;
      agent.log("External blackout - supply forced to 0");
    }

    ACLMessage proposal = cfp.createReply();
    proposal.setPerformative(ACLMessage.PROPOSE);
    proposal.setOntology(ONT_PROPOSAL);
    // inReplyTo helps aggregator know which CFP we’re responding to
    proposal.setInReplyTo("CNP_SHORTFALL");

    if (canSupply > 0) {
      proposal.setContent("supply=" + canSupply + ";cost=" + agent.cost);
    } else {
      // we have 0 capacity or can't supply => respond with high cost
      proposal.setContent("supply=0;cost=9999");
    }
    agent.send(proposal);
  }

  private void handleDecision(ACLMessage msg) {
    if (ONT_ACCEPT.equals(msg.getOntology())) {
      double accepted = parseAccepted(msg.getContent());
      agent.log("External source supplying " + accepted + " kW this tick.");
      agent.reportState(0.0, accepted, 0.0);
    } else if (ONT_REJECT.equals(msg.getOntology())) {
      agent.log("Proposal from external source was rejected: " + msg.getContent());
      agent.reportState(0.0, 0.0, 0.0);
    }
  }

  private double parseAccepted(String content) {
    double amt = 0;
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
