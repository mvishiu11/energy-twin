package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractSimAgent;
import com.energytwin.microgrid.core.behaviours.TickSubscriberBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.energytwin.microgrid.agentfusion.util.SpringContext;
import com.energytwin.microgrid.service.SimulationConfigService;

/**
 * Energy Storage Agent that receives energy allocation messages.
 */
public class EnergyStorageAgent extends AbstractSimAgent {

    private double capacity;
    private double currentStored = 0.0;

    @Override
    protected void setup() {
        initSpring();
        // Load configuration for this agent
        SimulationConfigService configService = SpringContext.getApplicationContext().getBean(SimulationConfigService.class);
        // For demonstration, use a default capacity. In practice, lookup by agent name.
        this.capacity = 200.0;
        log("Energy Storage Agent started with capacity: " + capacity);

        // Add behaviour to subscribe to tick messages (if needed) and to listen for allocation messages.
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchOntology("ENERGY_ALLOCATION");
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    try {
                        double allocated = Double.parseDouble(msg.getContent());
                        log("Received allocation: " + allocated);
                        // Simple storage logic: add allocation but do not exceed capacity.
                        currentStored = Math.min(currentStored + allocated, capacity);
                        log("Current stored energy: " + currentStored);
                    } catch (NumberFormatException e) {
                        log("Error parsing allocation message: " + msg.getContent());
                    }
                } else {
                    block();
                }
            }
        });

        // Optionally, subscribe to tick messages if you want to update storage state per tick.
        addBehaviour(new TickSubscriberBehaviour(this));
    }

    @Override
    public void onTick(long simulationTime) {
        // For now, storage agent does not need to act on tick directly.
    }
}
