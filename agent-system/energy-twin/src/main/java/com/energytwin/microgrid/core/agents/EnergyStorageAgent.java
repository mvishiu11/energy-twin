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
    protected void onAgentSetup() {
        initSpring();
        this.capacity = 200.0;
        log("Energy Storage Agent started with capacity: " + capacity);

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchOntology("ENERGY_ALLOCATION");
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    try {
                        double allocated = Double.parseDouble(msg.getContent());
                        log("Received allocation: " + allocated);
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

        addBehaviour(new TickSubscriberBehaviour(this));
    }

    @Override
    public void onTick(long simulationTime) {
        // For now, storage agent does not need to act on tick directly.
    }
}
