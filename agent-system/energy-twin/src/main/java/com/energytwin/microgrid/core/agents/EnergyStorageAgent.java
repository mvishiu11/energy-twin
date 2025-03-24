package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractEnergyStorageAgent;
import com.energytwin.microgrid.core.behaviours.energy.EnergyStorageBehaviour;
import java.util.Map;

/** Energy Storage Agent that receives energy allocation messages. */
public class EnergyStorageAgent extends AbstractEnergyStorageAgent {

  @Override
  protected void onAgentSetup() {
    setConfigParams();

    addBehaviour(new EnergyStorageBehaviour(this));
  }

  protected void setConfigParams() {
    String type = "energyStorage";
    String param = "capacity";
    Map<String, Object> myConfig =
        simulationConfigService.findAgentDefinition(type, getLocalName());
    if (myConfig != null) {
      Object rateObj = myConfig.get(param);
      if (rateObj != null) {
        try {
          this.capacity = Double.parseDouble(rateObj.toString());
        } catch (NumberFormatException e) {
          log("Error parsing " + param + ": " + rateObj, e);
        }
      }
    } else {
      log("No configuration found for agent of type " + type + " with name: " + getLocalName());
    }
    log("Energy Storage Agent started with " + param + ": " + capacity);
  }
}
