package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractEnergyStorageAgent;
import com.energytwin.microgrid.core.behaviours.energy.BatteryCNPResponder;
import com.energytwin.microgrid.core.behaviours.tick.TickSubscriberBehaviour;
import jade.core.AID;
import java.util.Map;

public class EnergyStorageAgent extends AbstractEnergyStorageAgent {

  @Override
  protected void onAgentSetup() {
    setConfigParams();

    // Add a behaviour to respond to CFP (shortfall or surplus)
    AID shortfallTopic = new AID("CNP_SHORTFALL_TOPIC", AID.ISLOCALNAME);
    AID surplusTopic = new AID("CNP_SURPLUS_TOPIC", AID.ISLOCALNAME);
    AID tickTopic = new AID("TICK_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new BatteryCNPResponder(this, shortfallTopic, surplusTopic));
    addBehaviour(new TickSubscriberBehaviour(this, tickTopic));
    log("Energy Storage Agent started with capacity=" + capacity + " cost=" + cost);
  }

  @Override
  protected void setConfigParams() {
    String type = "energyStorage";
    Map<String, Object> myConfig =
        simulationConfigService.findAgentDefinition(type, getLocalName());
    if (myConfig != null) {
      // parse capacity
      Object capObj = myConfig.get("capacity");
      if (capObj != null) {
        try {
          this.capacity = Double.parseDouble(capObj.toString());
        } catch (NumberFormatException e) {
          log("Error parsing capacity: " + capObj, e);
        }
      }
      // parse cost
      Object costObj = myConfig.get("cost");
      if (costObj != null) {
        try {
          this.cost = Double.parseDouble(costObj.toString());
        } catch (NumberFormatException e) {
          log("Error parsing cost: " + costObj, e);
        }
      }
      // parse initial SoC if needed
      Object socObj = myConfig.get("initialSoC");
      if (socObj != null) {
        try {
          this.currentSoC = Double.parseDouble(socObj.toString());
        } catch (NumberFormatException e) {
          log("Error parsing initialSoC: " + socObj, e);
        }
      }
    } else {
      log("No configuration found for agent of type " + type + " with name: " + getLocalName());
    }
  }

  @Override
  public void onTick(long simulationTime) {
    log("Current SoC: " + this.getAvailableToDischarge() + " at tick: " + simulationTime);

    reportState(0.0, 0.0, this.getAvailableToDischarge());
  }
}
