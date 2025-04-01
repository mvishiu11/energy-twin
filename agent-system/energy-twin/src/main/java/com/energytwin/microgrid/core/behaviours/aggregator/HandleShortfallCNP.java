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

public class HandleShortfallCNP extends OneShotBehaviour {
  private final double shortfall;
  private final AggregatorAgent agent;
  private final AID shortfallTopic;

  public HandleShortfallCNP(AggregatorAgent agent, double shortfall, AID shortfallTopic) {
    super(agent);
    this.shortfall = shortfall;
    this.agent = agent;
    this.shortfallTopic = shortfallTopic;
  }

  @Override
  public void action() {
    agent.log("SHORTFALL detected: " + shortfall + " kW. Attempting battery discharge first.");

    // 1) Send CFP to all storage + external agents for shortfall

    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    cfp.setOntology(ONT_CFP_SHORTFALL);
    cfp.setContent(String.valueOf(shortfall));
    cfp.addReceiver(shortfallTopic);

    myAgent.send(cfp);

    // 2) Wait for proposals.
    //    Ideally, we'd use an FSM or CyclicBehaviour to handle multiple messages.

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      agent.log("Interrupted while waiting for proposals", e);
    }

    // 3) Collect proposals
    List<Proposal> proposals = collectProposals(ONT_PROPOSAL, ONT_CFP_SHORTFALL);

    // 4) Sort them: by cost ascending, tie-break by largest supply, then first-respond.
    proposals.sort(
        (p1, p2) -> {
          int costCompare = Double.compare(p1.getCost(), p2.getCost());
          if (costCompare != 0) {
            return costCompare; // lower cost first
          } else {
            // tie-break: largest supply
            int supplyCompare = Double.compare(p2.getAmount(), p1.getAmount());
            if (supplyCompare != 0) {
              return supplyCompare; // bigger supply first
            } else {
              // tie-break: by arrival index
              return Integer.compare(p1.getArrivalIndex(), p2.getArrivalIndex());
            }
          }
        });

    double remainingShortfall = shortfall;
    List<Proposal> accepted = new ArrayList<>();

    for (Proposal p : proposals) {
      if (remainingShortfall <= 0) break;
      double used = Math.min(remainingShortfall, p.getAmount());
      remainingShortfall -= used;
      p.setAcceptedAmount(used);
      accepted.add(p);
    }

    // 5) Accept or reject
    for (Proposal p : proposals) {
      if (accepted.contains(p)) {
        // Accept
        ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        accept.setOntology(ONT_ACCEPT);
        accept.setInReplyTo(ONT_CFP_SHORTFALL);
        accept.setContent("acceptedAmount=" + p.getAcceptedAmount());
        accept.addReceiver(p.getSender());
        agent.log("Accepted proposal from: " + p.getSender() + " to " + p.getAcceptedAmount());
        agent.send(accept);
      } else {
        // Reject
        ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
        reject.setOntology(ONT_REJECT);
        reject.setContent("reason=better offers or shortfall satisfied");
        reject.addReceiver(p.getSender());
        agent.send(reject);
      }
    }

    double shortfallAfterBatteries = remainingShortfall;
    if (shortfallAfterBatteries > 0) {
      agent.log(
          "Still "
              + shortfallAfterBatteries
              + " kW shortfall after battery discharge. Logging partial blackout or future logic to request external source if not included above.");
    } else {
      agent.log("Shortfall satisfied by battery discharge (and/or external if it responded).");
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
