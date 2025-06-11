package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.aggregator.AggregatorMetaStore;
import com.energytwin.microgrid.core.base.AbstractSimAgent;
import com.energytwin.microgrid.core.behaviours.aggregator.HandleShortfallCNP;
import com.energytwin.microgrid.core.behaviours.aggregator.HandleSurplusCNP;
import com.energytwin.microgrid.core.behaviours.aggregator.ProductionConsumptionListener;
import com.energytwin.microgrid.core.behaviours.tick.TickSubscriberBehaviour;
import com.energytwin.microgrid.core.forecast.ProbabilisticForecaster;
import com.energytwin.microgrid.core.history.HistoryBuffer;
import com.energytwin.microgrid.core.planner.Action;
import com.energytwin.microgrid.core.planner.ActionQueue;
import com.energytwin.microgrid.core.planner.DeterministicPlanner;
import com.energytwin.microgrid.core.scenario.MonteCarloGenerator;
import com.energytwin.microgrid.core.scenario.QuantileTreeGenerator;
import com.energytwin.microgrid.core.scenario.Scenario;
import com.energytwin.microgrid.core.scenario.ScenarioGenerator;
import com.energytwin.microgrid.ws.dto.TickDataMessage;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AggregatorAgent extends AbstractSimAgent {

  /* ---------------- cumulative per-tick ---------------- */
  public double totalProductionThisTick = 0;
  public double totalConsumptionThisTick = 0;
  public Map<String, Double> productionMap = new HashMap<>();
  public Map<String, Double> consumptionMap = new HashMap<>();

  /* ---------------- forecasting buffers ---------------- */
  private HistoryBuffer hist;
  private AggregatorMetaStore meta;
  private int H_hist;
  private ProbabilisticForecaster forecaster;
  private final ActionQueue queue = new ActionQueue();
  private int H_pred;                 // horizon
  private int planEvery;              // re-plan cadence (ticks)
  private double epsilonBreak;        // Threshold to fallback to live-CNP
  private double[] planLoad;          // length H_pred
  private double[] planPv;
  private int  planPtr = 0;

  private int ticksSincePlan = 0;

  private ScenarioGenerator scenGen;
  private List<Scenario>   scenarios = List.of();

  private volatile double latestG = 0.0;   // W m⁻²
  private volatile double latestTa = 20.0; // °C
  private volatile double net;

  /* ---------------- ontology constants ---------------- */
  public static final String ONT_PRODUCTION   = "ENERGY_PRODUCTION";
  public static final String ONT_CONSUMPTION  = "ENERGY_CONSUMPTION";
  public static final String ONT_CFP_SHORTFALL= "CNP_SHORTFALL";
  public static final String ONT_CFP_SURPLUS  = "CNP_SURPLUS";
  public static final String ONT_PROPOSAL     = "CNP_PROPOSAL";
  public static final String ONT_ACCEPT       = "CNP_ACCEPT";
  public static final String ONT_REJECT       = "CNP_REJECT";

  /* ---------------- topic constants ---------------- */
  public static final String CNP_SHORTFALL_TOPIC = "CNP_SHORTFALL_TOPIC";
  public static final String CNP_SURPLUS_TOPIC = "CNP_SURPLUS_TOPIC";
  public static final String IRRADIANCE_TOPIC = "IRRADIANCE_TOPIC";

  private AID shortfallTopic, surplusTopic, irrTopic;

  /* ====================================================================== */
  @Override protected void onAgentSetup() {

    log("AggregatorAgent started.");

    /* ------------ topics ------------ */
    try {
      TopicManagementHelper tph = (TopicManagementHelper) getHelper(
              TopicManagementHelper.SERVICE_NAME);

      shortfallTopic = tph.createTopic(CNP_SHORTFALL_TOPIC);
      surplusTopic   = tph.createTopic(CNP_SURPLUS_TOPIC);
      irrTopic   = tph.createTopic(IRRADIANCE_TOPIC);

      tph.register(shortfallTopic);
      tph.register(surplusTopic);
      tph.register(irrTopic);

    } catch (Exception e) {
      log("Topic init error: {}", e.getMessage(), e);
      doDelete();  return;
    }

    /* ------------ forecast buffers & meta ------------ */
    Map<String,Object> fp = simulationConfigService.getForecastParams();
    H_hist    = (int) fp.getOrDefault("H_hist", 24);
    H_pred    = (int) fp.getOrDefault("H_pred", 4);
    planEvery = (int) fp.getOrDefault("replanEvery", 2);
    epsilonBreak = (double) fp.getOrDefault("epsilonBreak", 20.0);
    hist = new HistoryBuffer(H_hist);
    meta = new AggregatorMetaStore();
    loadBatteryMetaFromConfig();
    boolean useMc = (int) simulationConfigService.getForecastParams()
            .getOrDefault("useMC",0) == 1;
    scenGen = useMc ? new MonteCarloGenerator(H_pred,50)
            : new QuantileTreeGenerator(H_pred);

    forecaster = new ProbabilisticForecaster(H_pred);
    planLoad = new double[H_pred];
    planPv   = new double[H_pred];

    /* ------------ standard behaviours ------------ */
    AID tickTopic = new AID("TICK_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new TickSubscriberBehaviour(this, tickTopic));
    addBehaviour(new ProductionConsumptionListener(this));
    addBehaviour(new IrradianceListener());
  }

  /* ====================================================================== */
  @Override public void onTick(long simulationTime) {
    if (hist.isFull() && planPtr < planLoad.length) {
      registry.setForecast(planLoad[planPtr],          // kW load prediction
              planPv  [planPtr]);         // kW pv  prediction
    } else {
      registry.setForecast(0, 0);                      // “no forecast yet”
    }

    if (!queue.isEmpty()) {
      Action a = queue.pop();                // tickOffset == 0
      dispatch(a);                           // send ACCEPT_PROPOSALs
    }

    /* ----------- decide via CNP ----------- */
    net = totalProductionThisTick - totalConsumptionThisTick;
    log("Tick {}  P={}  L={}  Net={}", simulationTime,
            totalProductionThisTick, totalConsumptionThisTick, net);

    if (net < 0)
      addBehaviour(new HandleShortfallCNP(this, -net, shortfallTopic));
    else if (net > 0)
      addBehaviour(new HandleSurplusCNP(this,  net, surplusTopic));

    /* ----------- push to history buffer ----------- */
    double totSoc = meta.allBatteries().keySet().stream()
            .mapToDouble(n -> registry.all()
                    .getOrDefault(n,new TickDataMessage.AgentState())
                    .getStateOfCharge())
            .sum();

    /* ===== add sample to history ===== */
    hist.push(totalConsumptionThisTick, totalProductionThisTick,
            latestG, latestTa, totSoc);

    planPtr++;
    if (planPtr >= H_pred) planPtr = H_pred - 1;   // clamp

    ticksSincePlan = (ticksSincePlan + 1) % planEvery;
    if (hist.isFull() && ticksSincePlan == 0) {

      /* ------------------- fit models ------------------- */
      forecaster.update(hist.getLoad(),                // kW
              hist.getPv(),                  // kW
              hist.getTemp());               // °C

      /* ------------------- get forecasts ---------------- */
      double[][] loadQ = forecaster.predictLoad();     // [q05,q50,q95][H_pred]
      double[][] pvQ   = forecaster.predictPv();

      // build discrete scenarios
      scenarios = scenGen.generate(loadQ,pvQ);
      log("Fan chart: " + Arrays.toString(loadQ[0]) + " Q95: " + Arrays.toString(loadQ[2]));

      // push q05 & q95 to UI (fan chart)
      registry.setFanChart(loadQ[0], loadQ[2]);

      DeterministicPlanner planner = new DeterministicPlanner(
              H_pred, meta.allBatteries(), simulationConfigService.getExternalSourceCap());

      double socNow = meta.allBatteries().keySet().stream()
              .mapToDouble(id -> registry.all()
                      .getOrDefault(id,new TickDataMessage.AgentState()).getStateOfCharge())
              .sum();

      List<Action> plan = planner.solve(scenarios.get(1), socNow);
      queue.clear(); queue.addAll(plan);
      log("Planned "+plan.size()+" actions for next "+H_pred+" ticks.");

      // select median scenario for the deterministic planner (stage 4)
      planLoad = loadQ[1];
      planPv   = pvQ[1];
      planPtr  = 0;

      // immediate push so the UI sees it instantly
      registry.setForecast(planLoad[0], planPv[0]);

      log("New plan  L=" + Arrays.toString(planLoad) +
              "  PV=" + Arrays.toString(planPv));
    }

    /* reset per-tick accumulators --------------------------------- */
    totalProductionThisTick  = 0;
    totalConsumptionThisTick = 0;
    productionMap.clear();
    consumptionMap.clear();
  }

  /* ------------------------------------------------------------------ */
  /* LOAD ALL STATIC CAPABILITIES DIRECTLY FROM SIMULATION CONFIG       */
  private void loadBatteryMetaFromConfig() {

    simulationConfigService.getValidatedAgentDefinitions().stream()
            .filter(a -> "energyStorage".equalsIgnoreCase((String) a.get("type")))
            .forEach(a -> {
              String name = (String) a.get("name");
              double cap  = dbl(a, "capacity",      0.0);
              double ec   = dbl(a, "etaCharge",     0.95);
              double ed   = dbl(a, "etaDischarge",  0.93);
              double cr   = dbl(a, "cRate",         0.5);

              meta.addBattery(name,
                      new AggregatorMetaStore.BatteryMeta(cap, ec, ed, cr));
              log("Loaded meta from config for battery {}", name);
            });
  }

  private static double dbl(Map<String, Object> m, String k, double d){
    Object v = m.get(k); return v==null ? d : Double.parseDouble(v.toString());
  }

  private void dispatch(Action a){
    double plannedNet = planLoad[planPtr] - planPv[planPtr];
    if (Math.abs(net - plannedNet) > epsilonBreak) {
      queue.clear();
      planPtr = H_pred;
      log("Plan aborted – ε-break ("+epsilonBreak+" kW).");
      return;
    }
    if ("External".equals(a.target())){
      ACLMessage acc = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
      acc.setOntology(ONT_ACCEPT);
      acc.setInReplyTo(ONT_CFP_SHORTFALL);
      acc.setContent("acceptedAmount=" + Math.abs(a.extImportKw()));
      acc.addReceiver(new AID("ExternalSupply", AID.ISLOCALNAME));
      send(acc);
    } else {
      ACLMessage acc = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
      acc.setOntology(ONT_ACCEPT);
      acc.setInReplyTo(a.chargeKw()>0 ? ONT_CFP_SURPLUS : ONT_CFP_SHORTFALL);
      acc.setContent("acceptedAmount=" + Math.abs(a.chargeKw()));
      acc.addReceiver(new AID(a.target(), AID.ISLOCALNAME));
      send(acc);
    }
  }

  private final class IrradianceListener extends CyclicBehaviour {
    @Override public void action() {
      ACLMessage m = myAgent.receive(MessageTemplate.MatchOntology("IRRADIANCE"));
      if (m == null) { block(); return; }

      double G = 0, Ta = 20;
      for (String tok : m.getContent().split(";")) {
        String[] kv = tok.split("=");
        if (kv.length != 2) continue;
        if ("G".equals(kv[0]))  G  = Double.parseDouble(kv[1]);
        if ("T".equals(kv[0]))  Ta = Double.parseDouble(kv[1]);
      }
      latestG  = G;
      latestTa = Ta;
    }
  }
}
