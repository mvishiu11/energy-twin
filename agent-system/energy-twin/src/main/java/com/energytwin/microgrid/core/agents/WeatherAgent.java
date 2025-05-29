package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.base.AbstractSimAgent;
import com.energytwin.microgrid.core.behaviours.tick.TickSubscriberBehaviour;
import jade.core.AID;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import java.util.Map;
import java.util.Random;

/**
 * Generates global-horizontal irradiance [W m⁻²] and ambient temperature [°C]
 * from a pseudo-realistic solar/temperature model and broadcasts them each tick.
 *
 * Required JSON under "weather" in SimulationConfigService:
 *   sunriseTick   int   (e.g.  6)
 *   sunsetTick    int   (e.g. 18)
 *   sunPeakTick   int   (e.g. 12)
 *   gPeak         double  (max clear-sky irradiance, e.g. 1000)
 *   tempMeanDay   double  (mean day °C)
 *   tempMeanNight double  (mean night °C)
 *   tempMinTick   int     (hour of daily Tmin, default 5)
 *   sigmaG        double  (cloud std-dev fraction, default 0.15)
 *   sigmaT        double  (temperature noise °C, default 0.8)
 */
public final class WeatherAgent extends AbstractSimAgent {

  // simulation parameters
  private int sunriseTick;      // hour 0-23
  private int sunsetTick;       // hour 0-23
  private int sunPeakTick;      // hour of max irradiance
  private double gPeak;         // W/m²
  private double tempMeanDay;   // °C
  private double tempMeanNight; // °C
  private int tempMinTick;      // hour of daily minimum temperature
  private double sigmaG;        // relative STD for G
  private double sigmaT;        // °C STD for temp

  private long cachedVersion;   // weather version
  private AID irradianceTopic;
  private final Random rnd = new Random();

  @Override
  protected void onAgentSetup() {
    readConfig();
    cachedVersion = simulationConfigService.getWeatherVersion();

    try {
      TopicManagementHelper helper =
              (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
      AID tickTopic = helper.createTopic("TICK_TOPIC");
      irradianceTopic = helper.createTopic("IRRADIANCE_TOPIC");
      helper.register(irradianceTopic);
      addBehaviour(new TickSubscriberBehaviour(this, tickTopic));
      log("WeatherAgent initialised: sunrise=%02d sunset=%02d peak=%02d  G_peak=%.0f  T_day=%.1f °C  T_night=%.1f °C"
              .formatted(sunriseTick, sunsetTick, sunPeakTick, gPeak, tempMeanDay, tempMeanNight));
    } catch (Exception e) {
      log("WeatherAgent topic error: {}", e.getMessage(), e);
      doDelete();
    }
  }

  /** Read weather parameters (with defaults) from SimulationConfigService. */
  private void readConfig() {
    Map<String, Object> weather = simulationConfigService.getWeatherParams();

    sunriseTick   = getInt(weather,  "sunriseTick",   6);
    sunsetTick    = getInt(weather,  "sunsetTick",   18);
    sunPeakTick   = getInt(weather,  "sunPeakTick",  12);
    gPeak         = getDbl(weather,  "gPeak",      1000.0);

    tempMeanDay   = getDbl(weather,  "tempMeanDay",   25.0);
    tempMeanNight = getDbl(weather,  "tempMeanNight", 15.0);
    tempMinTick   = getInt(weather,  "tempMinTick",    5);

    sigmaG        = getDbl(weather,  "sigmaG",      0.15);
    sigmaT        = getDbl(weather,  "sigmaT",      0.8);
  }

  @Override
  public void onTick(long tick) {
    long v = simulationConfigService.getWeatherVersion();
    if (v != cachedVersion) {
      readConfig();
      cachedVersion = v;
      log("Weather parameters reloaded (version {})", v);
    }

    double G = computeIrradiance((int) (tick % 24));
    double Ta = computeAmbientTemp((int) (tick % 24));

    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setOntology("IRRADIANCE");
    msg.setContent("G=" + G + ";T=" + Ta);
    msg.addReceiver(irradianceTopic);
    send(msg);

    log("t=%d  G=%.1f W/m²  Ta=%.1f °C".formatted(tick, G, Ta));
  }

  /* ---------- physics helpers ---------- */

  private double computeIrradiance(int hour) {
    if (hour < sunriseTick || hour > sunsetTick) return 0.0;
    double rel = (double) (hour - sunriseTick) / (sunsetTick - sunriseTick);
    double base = gPeak * Math.sin(Math.PI * rel);            // clear-sky sine model
    double noise = 1.0 + rnd.nextGaussian() * sigmaG;         // cloud noise
    return Math.max(0, base * noise);
  }

  private double computeAmbientTemp(int hour) {
    // Two-level sinusoid between mean night and mean day
    double mean = (tempMeanDay + tempMeanNight) / 2.0;
    double amp  = (tempMeanDay - tempMeanNight) / 2.0;
    double phase = Math.sin(Math.PI * (hour - tempMinTick) / 12.0);
    return mean + amp * phase + rnd.nextGaussian() * sigmaT;
  }

  private int getInt(Map<String, Object> m, String k, int def) {
    Object v = (m != null ? m.get(k) : null);
    return v == null ? def : Integer.parseInt(v.toString());
  }
  private double getDbl(Map<String, Object> m, String k, double def) {
    Object v = (m != null ? m.get(k) : null);
    return v == null ? def : Double.parseDouble(v.toString());
  }
}
