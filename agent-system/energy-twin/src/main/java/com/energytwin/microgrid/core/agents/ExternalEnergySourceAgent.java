package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.agentfusion.SpringAgent;
import com.energytwin.microgrid.core.behaviours.energy.ExternalSourceCNPResponder;
import jade.core.AID;

public class ExternalEnergySourceAgent extends SpringAgent {

  public double maxSupplyPerTick;
  public double cost;

  @Override
  protected void onAgentSetup() {
    log("ExternalEnergySourceAgent started.");
    setConfigParams();

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
}
