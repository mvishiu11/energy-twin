package com.energytwin.microgrid.core.behaviours.energy;

import com.energytwin.microgrid.agentfusion.util.SpringContext;
import com.energytwin.microgrid.core.agents.EnergyStorageAgent;
import com.energytwin.microgrid.core.base.AbstractEnergyStorageAgent;
import com.energytwin.microgrid.service.EventControlService;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/** Responds to shortfall/surplus CFPs with physically valid proposals. */
public final class BatteryCNPResponder extends CyclicBehaviour {

  private final AbstractEnergyStorageAgent bat;
  private final EventControlService controlService;
  private static final String CFP_SHORT = "CNP_SHORTFALL";
  private static final String CFP_SURPL = "CNP_SURPLUS";
  private static final String ONT_PROP  = "CNP_PROPOSAL";
  private static final String ONT_ACCEPT = "CNP_ACCEPT";
  private static final String ONT_REJECT = "CNP_REJECT";

  public BatteryCNPResponder(AbstractEnergyStorageAgent bat, AID shortfallTopic, AID surplusTopic) {
    super(bat);
    this.bat = bat;
    this.controlService = SpringContext.getBean(EventControlService.class);
    try {
      TopicManagementHelper h = (TopicManagementHelper)
              bat.getHelper(TopicManagementHelper.SERVICE_NAME);
      h.register(shortfallTopic); h.register(surplusTopic);
    } catch (Exception e) { bat.log("Topic register error: {}", e.getMessage(), e); }
  }

  @Override
  public void action() {

    if(controlService.isBroken(bat.getLocalName())){
      block();
      return;
    }

    ACLMessage msg = myAgent.receive(MessageTemplate.or(
            MessageTemplate.MatchOntology(CFP_SHORT),
            MessageTemplate.MatchOntology(CFP_SURPL)));

    if (msg != null) { respondToCFP(msg); return; }

    ACLMessage dec = myAgent.receive(MessageTemplate.or(
            MessageTemplate.MatchOntology(ONT_ACCEPT),
            MessageTemplate.MatchOntology(ONT_REJECT)));

    if (dec != null) { handleDecision(dec); return; }

    block();
  }

  /* ---------- CFP ---------- */
  private void respondToCFP(ACLMessage cfp) {
    String ont = cfp.getOntology();
    double req = Double.parseDouble(cfp.getContent());   // kWh demand or surplus

    ACLMessage prop = cfp.createReply();
    prop.setPerformative(ACLMessage.PROPOSE);
    prop.setOntology(ONT_PROP);
    prop.setInReplyTo(ont);

    if (CFP_SHORT.equals(ont)) {          // discharge request
      double avail = bat.getAvailableToDischarge();          // kWh deliverable
      double supply = Math.min(avail, req);
      double cost = 1.0 - bat.dischargeEffEff();             // fractional loss
      prop.setContent("supply=" + supply + ";cost=" + cost);

    } else {                              // surplus storage request
      double room = bat.getAvailableToCharge();              // kWh storable (grid view)
      double store = Math.min(room, req);
      double cost = 1.0 - bat.chargeEffEff();
      prop.setContent("store=" + store + ";cost=" + cost);
    }
    ((EnergyStorageAgent) bat).incrementCnpNegotiations();
    bat.send(prop);
    bat.log("Proposal sent: " + prop.getContent());
  }

  /* ---------- Accept / Reject ---------- */
  private void handleDecision(ACLMessage dec) {

    boolean accepted = ONT_ACCEPT.equals(dec.getOntology());
    if (!accepted) {                      // it is a REJECT
      bat.log("Proposal rejected: " + dec.getContent());
      return;
    }

    double amt = parseAccepted(dec.getContent());   // only for ACCEPT
    if (dec.getInReplyTo() == null || amt == 0.0) return;

    if (CFP_SHORT.equals(dec.getInReplyTo())) {          // discharge accepted
      double nd = bat.dischargeEffEff();
      double dsoc = amt / nd;
      ((EnergyStorageAgent)bat).setDsoc(dsoc);
      bat.setSocKwh(Math.max(0, bat.getSocKwh() - dsoc)); // dsoc dodatni - bateria oddala/ ujemny - przyjela
      bat.setSocKwh(Math.max(0, bat.getSocKwh() - dsoc));
      if (!Double.isFinite(bat.getSocKwh()))
        bat.setSocKwh(0);
      bat.log("Delivered %.2f kWh  ηd=%.2f  new SoC=%.2f"
              .formatted(amt, nd, bat.getSocKwh()));

    } else if (CFP_SURPL.equals(dec.getInReplyTo())) {   // charge accepted
      double nc = bat.chargeEffEff();
      double dsoc = amt * nc;
      bat.setSocKwh(Math.min(bat.getCapacityKwh(), bat.getSocKwh() + dsoc));
      bat.log("Stored %.2f kWh  ηc=%.2f  new SoC=%.2f"
              .formatted(amt, nc, bat.getSocKwh()));
    }
  }

  /* Parse "acceptedAmount=5.0" safely */
  private double parseAccepted(String content) {
    if (!content.startsWith("acceptedAmount=")) return 0;
    try {
      return Double.parseDouble(content.split("=")[1]);
    } catch (NumberFormatException e) {
      bat.log("Failed to parse accepted amount: " + content, e);
      return 0;
    }
  }
}
