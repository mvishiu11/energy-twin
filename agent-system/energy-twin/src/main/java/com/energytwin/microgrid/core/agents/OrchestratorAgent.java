package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.agentfusion.SpringAgent;
import com.energytwin.microgrid.core.behaviours.tick.TickBroadcastBehaviour;
import jade.core.AID;
import jade.core.messaging.TopicManagementHelper;

/**
 * OrchestratorAgent is responsible for broadcasting simulation tick messages to all subscribed
 * agents. It creates a tick topic using JADE's TopicManagementHelper and sends ticks only when the
 * simulation is active.
 */
public class OrchestratorAgent extends SpringAgent {

    @Override
  protected void onAgentSetup() {
    log("Orchestrator Agent started.");

      AID tickTopic;
      try {
      TopicManagementHelper topicHelper =
          (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
      tickTopic = topicHelper.createTopic("TICK_TOPIC");
      topicHelper.register(tickTopic);
      log("Tick topic created and registered: {}", tickTopic.getLocalName());
    } catch (Exception e) {
      log("Failed to create tick topic: {}", e.getMessage(), e);
      doDelete();
      return;
    }

        TickBroadcastBehaviour tickBroadcastBehaviour = new TickBroadcastBehaviour(
                this,
                simulationControlService.getSimulationDelay(),
                tickTopic,
                simulationControlService);
    addBehaviour(tickBroadcastBehaviour);
  }
}
