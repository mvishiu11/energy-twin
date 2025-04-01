package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.agentfusion.SpringAgent;
import com.energytwin.microgrid.core.behaviours.energy.ExternalSourceCNPResponder;
import jade.core.AID;
import java.util.Map;

public class ExternalEnergySourceAgent extends SpringAgent {

  public double maxSupplyPerTick = 100.0;
  public double cost = 5.0;

  @Override
  protected void onAgentSetup() {
    log("ExternalEnergySourceAgent started.");
    setConfigParams();

    // Add behaviour to respond to shortfall CFP
    AID shortfallTopic = new AID("CNP_SHORTFALL_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new ExternalSourceCNPResponder(this, shortfallTopic));
  }

  private void setConfigParams() {
    String type = "externalSource";
    Map<String, Object> myConfig =
        simulationConfigService.findAgentDefinition(type, getLocalName());
    if (myConfig != null) {
      Object supplyObj = myConfig.get("capacity");
      if (supplyObj != null) {
        try {
          maxSupplyPerTick = Double.parseDouble(supplyObj.toString());
        } catch (NumberFormatException e) {
          log("Error parsing capacity for external source", e);
        }
      }
      Object costObj = myConfig.get("cost");
      if (costObj != null) {
        try {
          cost = Double.parseDouble(costObj.toString());
        } catch (NumberFormatException e) {
          log("Error parsing cost", e);
        }
      }
    }
    log(
        "ExternalEnergySourceAgent config => maxSupplyPerTick="
            + maxSupplyPerTick
            + " cost="
            + cost);
  }
}
