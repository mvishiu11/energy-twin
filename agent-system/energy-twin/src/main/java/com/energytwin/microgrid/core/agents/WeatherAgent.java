package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractSimAgent;
import com.energytwin.microgrid.core.behaviours.tick.TickSubscriberBehaviour;
import jade.core.AID;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import java.util.Random;

/** Simulates and broadcasts irradiance data each tick. */
public class WeatherAgent extends AbstractSimAgent {

  private AID irradianceTopic;
  private final Random random = new Random();

  @Override
  protected void onAgentSetup() {
    log("WeatherAgent started");

    AID tickTopic;
    try {
      TopicManagementHelper helper =
          (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
      tickTopic = helper.createTopic("TICK_TOPIC");
      irradianceTopic = helper.createTopic("IRRADIANCE_TOPIC");
      helper.register(irradianceTopic);
    } catch (Exception e) {
      log("Error registering topics: {}", e.getMessage(), e);
      doDelete();
      return;
    }

    addBehaviour(new TickSubscriberBehaviour(this, tickTopic));
  }

  @Override
  public void onTick(long tickTime) {
    double irradiance = simulateIrradiance(tickTime);
    log("Broadcasting irradiance " + irradiance + " W/m² at tick " + tickTime);

    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setOntology("IRRADIANCE");
    msg.setContent(String.valueOf(irradiance));
    msg.addReceiver(irradianceTopic);
    send(msg);
  }

  private double simulateIrradiance(long tick) {
    double hours = tick; // 0-24h
    double base = Math.max(0, Math.sin(Math.PI * (hours - 6) / 12));
    double fluctuation = 1 + (random.nextGaussian());
    return base * 1000 * fluctuation; // ~0 to ~1000 W/m²
  }
}
