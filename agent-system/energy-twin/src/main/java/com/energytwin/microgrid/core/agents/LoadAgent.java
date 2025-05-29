package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractLoadAgent;
import com.energytwin.microgrid.core.behaviours.tick.TickSubscriberBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Map;
import java.util.Random;

/**
 * University-campus building load:
 * <ul>
 *   <li>Working 08–18 baseline 80 % P<sub>nom</sub></li>
 *   <li>Low-working 06–08, 18–20 baseline 50 %</li>
 *   <li>Night 20–06 baseline 10 %</li>
 *   <li>Random Δ ∈ [-0.10 , +0.30] clamped so load is
 *       never &lt; 0.10 P<sub>nom</sub> and never more than +30 pp above baseline.</li>
 * </ul>
 */
public final class LoadAgent extends AbstractLoadAgent {

  private static final Random RNG = new Random();

  @Override
  protected void onAgentSetup() {
    setConfigParams();

    AID tickTopic = new AID("TICK_TOPIC", AID.ISLOCALNAME);
    addBehaviour(new TickSubscriberBehaviour(this, tickTopic));
  }

  /** Parse nominal load; all defaults handled by config-service. */
  @Override
  protected void setConfigParams() {
    Map<String, Object> cfg =
            simulationConfigService.findAgentDefinition("load", getLocalName());

    if (cfg == null || !cfg.containsKey("nominalLoad")) {
      throw new IllegalArgumentException("Load agent " + getLocalName()
              + " missing 'nominalLoad' parameter in simulation config.");
    }
    nominalLoadKw = Double.parseDouble(cfg.get("nominalLoad").toString());
    log("Nominal load set to %.1f kW".formatted(nominalLoadKw));
  }

  /* ---------------- tick ---------------- */

  @Override
  public void onTick(long tick) {
    int rate = eventControlService.checkLoadSpike(getLocalName());
    int hour = (int) (tick % 24);
    double baselineFrac = baselineFraction(hour);

    // random Δ in [−0.10 , +0.30]
    double delta = -0.10 + 0.40 * RNG.nextDouble();
    double frac = baselineFrac + delta;

    // clamp within ±(10,30) pp of baseline
    frac = Math.max(baselineFrac - 0.10, frac);
    frac = Math.min(baselineFrac + 0.30, frac);

    // global floor / ceiling
    frac = Math.max(0.10, frac);
    frac = Math.min(1.30, frac);

    double consumedKw = frac * nominalLoadKw * rate;

    /* ---------- messaging ---------- */
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setOntology("ENERGY_CONSUMPTION");
    msg.setContent(String.valueOf(consumedKw));
    msg.addReceiver(new AID("AggregatorAgent", AID.ISLOCALNAME));
    send(msg);

    reportState(consumedKw, 0.0, 0.0);

    log("t=%d  hour=%02d  baseline=%.0f%%  Δ=%.1f%%  load=%.2f kW"
            .formatted(tick, hour, baselineFrac * 100, delta * 100, consumedKw));
  }

  /* ---------------- helpers ---------------- */

  private double baselineFraction(int hour) {
    if (hour >=  8 && hour < 18) return 0.80;   // working
    if ((hour >= 6 && hour < 8) || (hour >= 18 && hour < 20)) return 0.50; // low-working
    return 0.10;                                // night
  }
}
