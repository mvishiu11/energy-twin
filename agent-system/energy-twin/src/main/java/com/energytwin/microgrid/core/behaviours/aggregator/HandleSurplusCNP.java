package com.energytwin.microgrid.core.behaviours.aggregator;

import static com.energytwin.microgrid.core.agents.AggregatorAgent.*;

import com.energytwin.microgrid.core.agents.AggregatorAgent;
import com.energytwin.microgrid.core.models.Proposal;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.List;

public class HandleSurplusCNP extends OneShotBehaviour {
  private final double surplus;
  private final AggregatorAgent agent;
  private final AID surplusTopic;

  public HandleSurplusCNP(AggregatorAgent agent, double surplus, AID surplusTopic) {
    super(agent);
    this.surplus = surplus;
    this.agent = agent;
    this.surplusTopic = surplusTopic;
  }

  @Override
  public void action() {
    agent.log("SURPLUS detected: " + surplus + " kW. Attempting battery storage...");

    // 1) Send CFP to all storage agents for surplus
    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    cfp.setOntology(ONT_CFP_SURPLUS);
    cfp.setContent(String.valueOf(surplus));
    cfp.addReceiver(surplusTopic);

    agent.send(cfp);

    // 2) Wait for proposals
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      agent.log("Interrupted while waiting for surplus proposals", e);
    }

    // 3) Collect proposals
    List<Proposal> proposals = collectProposals(ONT_PROPOSAL, ONT_CFP_SURPLUS);

    // 4) Sort them by cost ascending, tie-break by largest capacity, then first respond
    proposals.sort(
        (p1, p2) -> {
          int costCompare = Double.compare(p1.getCost(), p2.getCost());
          if (costCompare != 0) {
            return costCompare;
          } else {
            // tie-break: bigger store capacity
            int storeCompare = Double.compare(p2.getAmount(), p1.getAmount());
            if (storeCompare != 0) {
              return storeCompare;
            } else {
              // tie-break: arrival index
              return Integer.compare(p1.getArrivalIndex(), p2.getArrivalIndex());
            }
          }
        });

    double leftover = surplus;
    List<Proposal> accepted = new ArrayList<>();

    for (Proposal p : proposals) {
      if (leftover <= 0) break;
      double used = Math.min(leftover, p.getAmount());
      leftover -= used;
      p.setAcceptedAmount(used);
      accepted.add(p);
    }

    // 5) Accept or reject
    for (Proposal p : proposals) {
      if (accepted.contains(p)) {
        // Accept
        ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        accept.setOntology(ONT_ACCEPT);
        accept.setInReplyTo(ONT_CFP_SURPLUS);
        accept.setContent("acceptedAmount=" + p.getAcceptedAmount());
        accept.addReceiver(p.getSender());
        agent.send(accept);
      } else {
        // Reject
        ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
        reject.setOntology(ONT_REJECT);
        reject.setContent("reason=better offers or surplus allocated");
        reject.addReceiver(p.getSender());
        agent.send(reject);
      }
    }

    if (leftover > 0) {
      agent.log("Not all surplus was stored. " + leftover + " kW wasted.");
    } else {
      agent.log("All surplus stored successfully.");
    }
  }

  /**
   * Collect proposals from the mailbox matching the given ontology for some small window. This is a
   * simplistic approach; a more robust approach might use an FSM or message-driven approach.
   */
  public List<Proposal> collectProposals(String proposalOntology, String cfpOntology) {
    List<Proposal> proposals = new ArrayList<>();
    long start = System.currentTimeMillis();
    int arrivalCounter = 0;

    while (System.currentTimeMillis() - start < 500) {
      ACLMessage msg =
          myAgent.receive(
              MessageTemplate.and(
                  MessageTemplate.MatchOntology(proposalOntology),
                  MessageTemplate.MatchInReplyTo(cfpOntology)));
      if (msg != null) {
        Proposal p = parseProposal(msg, arrivalCounter);
        proposals.add(p);
        arrivalCounter++;
      } else {
        block(50);
      }
    }
    return proposals;
  }

  /** Parse content like "supply=10.0;cost=2.5" or "store=5.0;cost=1.2" */
  public static Proposal parseProposal(ACLMessage msg, int arrivalIndex) {
    Proposal p = new Proposal();
    p.setSender(msg.getSender());
    p.setArrivalIndex(arrivalIndex);

    // example content "supply=10;cost=2.5" or "store=5;cost=3.0"
    String[] tokens = msg.getContent().split(";");
    for (String t : tokens) {
      String[] kv = t.split("=");
      if (kv.length == 2) {
        String key = kv[0].trim();
        String val = kv[1].trim();
        if ("supply".equalsIgnoreCase(key) || "store".equalsIgnoreCase(key)) {
          p.setAmount(Double.parseDouble(val));
        } else if ("cost".equalsIgnoreCase(key)) {
          p.setCost(Double.parseDouble(val));
        }
      }
    }
    return p;
  }
}
