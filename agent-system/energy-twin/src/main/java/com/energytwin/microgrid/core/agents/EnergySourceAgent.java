package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractEnergySourceAgent;
import com.energytwin.microgrid.core.behaviours.TickSubscriberBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.List;
import java.util.Map;

/**
 * Energy Source Agent that produces energy at each tick and broadcasts production.
 */
public class EnergySourceAgent extends AbstractEnergySourceAgent {

    @Override
    protected void onAgentSetup() {
        Map<String, Object> simConfig = (Map<String, Object>) simulationConfigService.getConfig().get("simulation");
        List<Map<String, Object>> agentsList = (List<Map<String, Object>>) simConfig.get("agents");
        for (Map<String, Object> agentDef : agentsList) {
            String type = (String) agentDef.get("type");
            String name = (String) agentDef.get("name");
            if ("energySource".equalsIgnoreCase(type) && getLocalName().equals(name)) {
                Object rateObj = agentDef.get("productionRate");
                if (rateObj != null) {
                    this.productionRate = Double.parseDouble(rateObj.toString());
                }
                break;
            }
        }
        log("Energy Source Agent started with production rate: " + productionRate);

        AID tickTopic = new AID("TickTopic", AID.ISLOCALNAME);
        addBehaviour(new TickSubscriberBehaviour(this, tickTopic));
    }

    @Override
    public void onTick(long simulationTime) {
        double produced = productionRate;
        log("Produced energy: " + produced + " kW at simulation time: " + simulationTime);

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setOntology("ENERGY_PRODUCTION");
        msg.setContent(String.valueOf(produced));
        msg.addReceiver(new jade.core.AID("AggregationTopic", jade.core.AID.ISLOCALNAME));
        send(msg);
    }
}
