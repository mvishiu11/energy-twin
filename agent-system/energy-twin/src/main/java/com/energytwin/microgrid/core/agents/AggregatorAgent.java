package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.agentfusion.SpringAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

/**
 * Aggregator Agent collects production and consumption data,
 * computes the remaining energy, and broadcasts allocation messages.
 */
public class AggregatorAgent extends SpringAgent {

    private double totalProduction = 0.0;
    private double totalConsumption = 0.0;

    @Override
    protected void onAgentSetup() {
        log("Aggregator Agent started.");

        // Register this agent in a topic "AggregationTopic" using TopicManagementHelper.
        // Add a cyclic behaviour to listen for production and consumption messages.
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // Listen for messages with ontologies "ENERGY_PRODUCTION" and "ENERGY_CONSUMPTION"
                MessageTemplate mtProd = MessageTemplate.MatchOntology("ENERGY_PRODUCTION");
                MessageTemplate mtCons = MessageTemplate.MatchOntology("ENERGY_CONSUMPTION");
                ACLMessage msg = myAgent.receive(MessageTemplate.or(mtProd, mtCons));
                if (msg != null) {
                    try {
                        double value = Double.parseDouble(msg.getContent());
                        if ("ENERGY_PRODUCTION".equals(msg.getOntology())) {
                            totalProduction += value;
                            log("Aggregator received production: " + value);
                        } else if ("ENERGY_CONSUMPTION".equals(msg.getOntology())) {
                            totalConsumption += value;
                            log("Aggregator received consumption: " + value);
                        }
                    } catch (NumberFormatException e) {
                        log("Error parsing message: " + msg.getContent());
                    }
                } else {
                    block();
                }
            }
        });

        // Add a tick subscriber to perform allocation at each tick.
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchOntology("TICK");
                ACLMessage tickMsg = myAgent.receive(mt);
                if (tickMsg != null) {
                    // On each tick, calculate allocation and broadcast to storage agents
                    double remaining = totalProduction - totalConsumption;
                    log("Tick received. Total Production: " + totalProduction + ", Total Consumption: " + totalConsumption + ", Remaining: " + remaining);
                    double allocation = remaining;
                    ACLMessage allocMsg = new ACLMessage(ACLMessage.INFORM);
                    allocMsg.setOntology("ENERGY_ALLOCATION");
                    allocMsg.setContent(String.valueOf(allocation));
                    // Send allocation to a topic "AllocationTopic"
                    allocMsg.addReceiver(new AID("AllocationTopic", AID.ISLOCALNAME));
                    send(allocMsg);

                    // Reset totals for next tick cycle
                    totalProduction = 0.0;
                    totalConsumption = 0.0;
                } else {
                    block();
                }
            }
        });
    }

    @Override
    public void onTick(long simulationTime) {
        // Not used directly; allocation is handled by the cyclic behaviour.
    }
}
