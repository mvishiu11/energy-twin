package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractSimAgent;
import com.energytwin.microgrid.core.behaviours.aggregator.HandleShortfallCNP;
import com.energytwin.microgrid.core.behaviours.aggregator.HandleSurplusCNP;
import com.energytwin.microgrid.core.behaviours.aggregator.ProductionConsumptionListener;
import com.energytwin.microgrid.core.behaviours.tick.TickSubscriberBehaviour;
import jade.core.AID;
import jade.core.messaging.TopicManagementHelper;
import java.util.*;

public class AggregatorAgent extends AbstractSimAgent {

  public double totalProductionThisTick = 0;
  public double totalConsumptionThisTick = 0;

  public Map<String, Double> productionMap = new HashMap<>();
  public Map<String, Double> consumptionMap = new HashMap<>();

  public static final String ONT_PRODUCTION = "ENERGY_PRODUCTION";
  public static final String ONT_CONSUMPTION = "ENERGY_CONSUMPTION";
  public static final String ONT_CFP_SHORTFALL = "CNP_SHORTFALL";
  public static final String ONT_CFP_SURPLUS = "CNP_SURPLUS";
  public static final String ONT_PROPOSAL = "CNP_PROPOSAL";
  public static final String ONT_ACCEPT = "CNP_ACCEPT";
  public static final String ONT_REJECT = "CNP_REJECT";
  private AID shortfallTopic, surplusTopic;

  @Override
  protected void onAgentSetup() {
    log("AggregatorAgent started.");

    try {
      TopicManagementHelper topicHelper =
          (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
      shortfallTopic = topicHelper.createTopic("CNP_SHORTFALL_TOPIC");
      surplusTopic = topicHelper.createTopic("CNP_SURPLUS_TOPIC");
      topicHelper.register(shortfallTopic);
      topicHelper.register(surplusTopic);
    } catch (Exception e) {
      log("Failed to create CNP topics: {}", e.getMessage(), e);
      doDelete();
      return;
    }

    AID tickTopic = new AID("TICK_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new TickSubscriberBehaviour(this, tickTopic));
    addBehaviour(new ProductionConsumptionListener(this));
  }

  @Override
  public void onTick(long simulationTime) {
    // 1) Compute net supply or net shortfall
    double net = totalProductionThisTick - totalConsumptionThisTick;

    log(
        "Tick "
            + simulationTime
            + " => Production="
            + totalProductionThisTick
            + ", Consumption="
            + totalConsumptionThisTick
            + ", Net="
            + net);

    if (net < 0) {
      // SHORTFALL scenario
      double shortfall = Math.abs(net);
      addBehaviour(new HandleShortfallCNP(this, shortfall, shortfallTopic));
    } else if (net > 0) {
      // SURPLUS scenario
      addBehaviour(new HandleSurplusCNP(this, net, surplusTopic));
    } else {
      log("Tick " + simulationTime + " => Perfectly balanced, as all things should be.");
    }

    // Reset for next tick
    totalProductionThisTick = 0;
    totalConsumptionThisTick = 0;
    productionMap.clear();
    consumptionMap.clear();
  }
}
