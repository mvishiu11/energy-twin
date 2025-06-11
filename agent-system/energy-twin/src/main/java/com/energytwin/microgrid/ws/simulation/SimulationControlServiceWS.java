package com.energytwin.microgrid.ws.simulation;

import com.energytwin.microgrid.registry.AgentStateRegistry;
import com.energytwin.microgrid.service.SimulationConfigService;
import com.energytwin.microgrid.ws.dto.MetricsMessage;
import com.energytwin.microgrid.ws.dto.TickDataMessage;
import com.energytwin.microgrid.ws.service.MetricsPublishingService;
import com.energytwin.microgrid.ws.service.TickPublishingService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service()
public class SimulationControlServiceWS {
    private final TickPublishingService tickPublisher;
    private final MetricsPublishingService metricsPublisher;
    private long tickCounter = 0;
    private final AgentStateRegistry registry;
    private final SimulationConfigService simulationConfigService;

    private final ScheduledExecutorService exec =
            Executors.newSingleThreadScheduledExecutor();

    private double cumulativeTotalDemand = 0.0;
    private double cumulativeGreenSupply = 0.0;

    private double totalDemandPerNTicks = 0.0;
    private double totalProducedPerNTicks = 0.0;

    private double cnpNegotiations = 0.0;

    public SimulationControlServiceWS(TickPublishingService tickPublisher, MetricsPublishingService metricsPublisher, AgentStateRegistry registry, SimulationConfigService simulationConfigService) {
        this.tickPublisher = tickPublisher;
        this.metricsPublisher = metricsPublisher;
        this.registry = registry;
        this.simulationConfigService = simulationConfigService;
    }

    public void onTickCompleted(){
        tickCounter++;

        // Gather and send data every tick
        TickDataMessage tickDataMessage = gatherTickData();

        double thisTickDemand = registry.getTotalEnergyDemand();
        double thisTickGreen =  registry.getTotalGreenEnergyGeneration();

        double predLoad = registry.getPredLoad();
        double predPv   = registry.getPredPv();

        tickDataMessage.setPredictedLoadKw(predLoad);
        tickDataMessage.setPredictedPvKw  (predPv);
        tickDataMessage.setErrorLoadKw(Math.abs(thisTickDemand - predLoad));
        tickDataMessage.setErrorPvKw(Math.abs(registry.getPvProduction() - predPv));

        tickDataMessage.setFanLo(registry.fanLo);
        tickDataMessage.setFanHi(registry.fanHi);

        tickPublisher.publish(tickDataMessage);

        cumulativeTotalDemand += thisTickDemand;
        cumulativeGreenSupply += thisTickGreen;

        totalProducedPerNTicks += thisTickGreen;
        totalDemandPerNTicks += thisTickDemand;

        cnpNegotiations += registry.getTotalCnpNegotiations();

        // Gather, compute and send metric every simulationConfigService.getMetricsPerNTicks() ticks
        if( tickCounter % simulationConfigService.getMetricsPerNTicks() == 0){
            MetricsMessage metrics = computeMetrics();
            metricsPublisher.publish(metrics);
            registry.resetErrorAccumulators();
        }
    }

    private TickDataMessage gatherTickData() {
        TickDataMessage msg = new TickDataMessage();
        msg.setTickNumber(tickCounter);

        Map<String, TickDataMessage.AgentState> snapshot = registry.all();
        msg.setAgentStates(snapshot);

        return msg;
    }

    private MetricsMessage computeMetrics() {
        MetricsMessage msg = new MetricsMessage();
        msg.setTickNumber(tickCounter);
        msg.setTotalConsumed(cumulativeTotalDemand);
        msg.setTotalProduced(cumulativeGreenSupply);
        msg.setTotalProducedPerNTicks(totalProducedPerNTicks);
        msg.setTotalDemandPerNTicks(totalDemandPerNTicks);
        msg.setCnpNegotiations(cnpNegotiations);
        msg.setForecastLoadKw(registry.getForecastLoad());
        msg.setForecastPvKw  (registry.getForecastPv());
        msg.setRmseLoadKw(registry.currentRmseLoad());
        msg.setRmsePvKw  (registry.currentRmsePv());

        double ratioPct = (cumulativeTotalDemand > 0) ? 100.0 * (cumulativeGreenSupply/cumulativeTotalDemand)
                : 0.0;
        msg.setGreenEnergyRatioPct(ratioPct);

        totalProducedPerNTicks = 0.0;
        totalDemandPerNTicks = 0.0;

        return msg;
    }
}
