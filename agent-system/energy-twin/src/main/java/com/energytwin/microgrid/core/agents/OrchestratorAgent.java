package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.agentfusion.SpringAgent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;

/**
 * Orchestrator Agent creates a tick topic and broadcasts tick messages
 * to all subscribed agents. It reads simulation control parameters from the
 * SimulationControlService.
 */
public class OrchestratorAgent extends SpringAgent {

    private AID tickTopic;
    private long simulationTime = 0;

    @Override
    protected void onAgentSetup() {
        initSpring();
        log("Orchestrator Agent started.");

        // Create the tick topic using JADE's TopicManagementHelper
        try {
            TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            tickTopic = topicHelper.createTopic("TickTopic");
            topicHelper.register(tickTopic);
            log("Tick topic created and registered: " + tickTopic.getLocalName());
        } catch (Exception e) {
            log("Failed to create tick topic: " + e.getMessage());
            e.printStackTrace();
            doDelete();
            return;
        }

        // Add a ticker behaviour to broadcast tick messages periodically
        addBehaviour(new TickerBehaviour(this, simulationControlService.getTickIntervalMillis()) {
            @Override
            protected void onTick() {
                if (simulationControlService.isPaused()) {
                    log("Simulation is paused; skipping tick.");
                    return;
                }
                // Advance simulation time based on speed-up factor
                simulationTime += simulationControlService.getSimulationTickIncrement();
                ACLMessage tickMsg = new ACLMessage(ACLMessage.INFORM);
                tickMsg.setOntology("TICK");
                tickMsg.setContent(String.valueOf(simulationTime));
                // Send message to the tick topic so all subscribers receive it
                tickMsg.addReceiver(tickTopic);
                send(tickMsg);
                log("Broadcasted tick: " + simulationTime);
            }
        });
    }

    @Override
    public void onTick(long simulationTime) {
        // Not used here since the orchestrator sends ticks.
    }
}
