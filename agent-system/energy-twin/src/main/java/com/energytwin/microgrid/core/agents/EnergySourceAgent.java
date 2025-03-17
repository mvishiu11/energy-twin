package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractEnergySourceAgent;
import com.energytwin.microgrid.core.behaviours.TickSubscriberBehaviour;
import com.energytwin.microgrid.service.SimulationConfigService;
import com.energytwin.microgrid.agentfusion.util.SpringContext;
import jade.lang.acl.ACLMessage;

import java.util.List;
import java.util.Map;

/**
 * Energy Source Agent that produces energy at each tick and broadcasts production.
 */
public class EnergySourceAgent extends AbstractEnergySourceAgent {

    @Override
    protected void setup() {
        initSpring();
        // Retrieve SimulationConfigService from Spring context.
        SimulationConfigService configService = SpringContext.getApplicationContext().getBean(SimulationConfigService.class);
        Map<String, Object> simConfig = (Map<String, Object>) configService.getConfig().get("simulation");
        List<Map<String, Object>> agentsList = (List<Map<String, Object>>) simConfig.get("agents");
        // Find this agent's configuration based on its name.
        for (Map<String, Object> agentDef : agentsList) {
            String type = (String) agentDef.get("type");
            String name = (String) agentDef.get("name");
            if ("energySource".equalsIgnoreCase(type) && getLocalName().equals(name)) {
                // Set production rate from configuration.
                Object rateObj = agentDef.get("productionRate");
                if (rateObj != null) {
                    this.productionRate = Double.parseDouble(rateObj.toString());
                }
                break;
            }
        }
        log("Energy Source Agent started with production rate: " + productionRate);

        // Add dedicated TickSubscriberBehaviour to subscribe to tick messages.
        addBehaviour(new TickSubscriberBehaviour(this));
    }

    @Override
    public void onTick(long simulationTime) {
        // Calculate production (could be dynamic using simulationTime)
        double produced = productionRate;
        log("Produced energy: " + produced + " kW at simulation time: " + simulationTime);

        // Create and send an ACL message for energy production
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setOntology("ENERGY_PRODUCTION");
        msg.setContent(String.valueOf(produced));
        // In a real system, send this to an aggregator topic/agent; here, we broadcast to a topic named "AggregationTopic"
        msg.addReceiver(new jade.core.AID("AggregationTopic", jade.core.AID.ISLOCALNAME));
        send(msg);
    }
}
