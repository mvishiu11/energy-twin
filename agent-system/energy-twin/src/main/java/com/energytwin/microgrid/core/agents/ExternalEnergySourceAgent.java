package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractSimAgent;
import com.energytwin.microgrid.core.behaviours.energy.ExternalSourceCNPResponder;
import com.energytwin.microgrid.core.behaviours.tick.TickSubscriberBehaviour;
import com.energytwin.microgrid.service.EventControlService;
import jade.core.AID;

public class ExternalEnergySourceAgent extends AbstractSimAgent {

  public double maxSupplyPerTick;
  public double cost;

  @Override
  protected void onAgentSetup() {
    log("ExternalEnergySourceAgent started.");
    setConfigParams();

    AID tickTopic = new AID("TICK_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new TickSubscriberBehaviour(this, tickTopic));

    // Add behaviour to respond to shortfall CFP
    AID shortfallTopic = new AID("CNP_SHORTFALL_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new ExternalSourceCNPResponder(this, shortfallTopic));
  }

  private void setConfigParams() {
    maxSupplyPerTick = simulationConfigService.getExternalSourceCap();
    cost = simulationConfigService.getExternalSourceCost();
    log(
        "ExternalEnergySourceAgent config => maxSupplyPerTick="
            + maxSupplyPerTick
            + " cost="
            + cost);
  }

  @Override
  public void onTick(long simulationTime) {
    eventControlService.inBlackoutAndTick();
    if (eventControlService.getBlackoutRemaining() > 0) {
      log("Blackout: remaining " + eventControlService.getBlackoutRemaining() + " ticks.");
    }
  }

  public EventControlService getEventControlService() {
    return this.eventControlService;
  }
}
