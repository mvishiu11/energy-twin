package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.agentfusion.SpringAgent;
import com.energytwin.microgrid.core.behaviours.TickBroadcastBehaviour;
import jade.core.AID;
import jade.core.messaging.TopicManagementHelper;

/**
 * OrchestratorAgent is responsible for broadcasting simulation tick messages to all subscribed
 * agents. It creates a tick topic using JADE's TopicManagementHelper and sends ticks only when the
 * simulation is active.
 */
public class OrchestratorAgent extends SpringAgent {

  private AID tickTopic;
  private TickBroadcastBehaviour tickBroadcastBehaviour;

  @Override
  protected void onAgentSetup() {
    log("Orchestrator Agent started.");

    try {
      TopicManagementHelper topicHelper =
          (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
      tickTopic = topicHelper.createTopic("TickTopic");
      topicHelper.register(tickTopic);
      log("Tick topic created and registered: {}", tickTopic.getLocalName());
    } catch (Exception e) {
      log("Failed to create tick topic: {}", e.getMessage(), e);
      doDelete();
      return;
    }

    tickBroadcastBehaviour =
        new TickBroadcastBehaviour(
            this,
            simulationControlService.getTickIntervalMillis(),
            tickTopic,
            simulationControlService);
    addBehaviour(tickBroadcastBehaviour);
  }

  @Override
  public void onTick(long simulationTime) {
    // Not used in the orchestrator.
  }
}
