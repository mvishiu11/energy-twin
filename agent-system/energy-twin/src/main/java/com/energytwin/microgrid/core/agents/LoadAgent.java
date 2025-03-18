package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractSimAgent;
import com.energytwin.microgrid.core.behaviours.TickSubscriberBehaviour;
import com.energytwin.microgrid.service.SimulationConfigService;
import com.energytwin.microgrid.agentfusion.util.SpringContext;
import jade.lang.acl.ACLMessage;

/**
 * Load Agent that consumes energy and broadcasts its consumption.
 */
public class LoadAgent extends AbstractSimAgent {

    private double consumptionRate;

    @Override
    protected void onAgentSetup() {
        initSpring();
        // Load configuration for this agent
        SimulationConfigService configService = SpringContext.getApplicationContext().getBean(SimulationConfigService.class);
        // For demonstration, set consumptionRate from config; here we use a default value.
        this.consumptionRate = 30.0;
        log("Load Agent started with consumption rate: " + consumptionRate);

        // Add tick subscription behaviour
        addBehaviour(new TickSubscriberBehaviour(this));
    }

    @Override
    public void onTick(long simulationTime) {
        double consumed = consumptionRate;
        log("Consumed energy: " + consumed + " kW at simulation time: " + simulationTime);

        // Create and send a consumption message
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setOntology("ENERGY_CONSUMPTION");
        msg.setContent(String.valueOf(consumed));
        msg.addReceiver(new jade.core.AID("AggregationTopic", jade.core.AID.ISLOCALNAME));
        send(msg);
    }
}
