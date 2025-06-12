package com.energytwin.microgrid.registry;

import com.energytwin.microgrid.ws.dto.TickDataMessage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

@Service
public class AgentStateRegistry {

    private final ConcurrentMap<String, TickDataMessage.AgentState> states = new ConcurrentHashMap<>();

    public void update(String agentName, TickDataMessage.AgentState state){
        states.put(agentName, state);
    }

    public Map<String, TickDataMessage.AgentState> all(){
        return states;
    }

    private volatile double[] latestForecastLoad = new double[0];
    private volatile double[] latestForecastPv   = new double[0];
    @Getter
    private volatile double[] fanLoPv = new double[0];
    @Getter
    private volatile double[] fanHiPv = new double[0];
    @Getter
    private volatile double[] fanLoLoad = new double[0];
    @Getter
    private volatile double[] fanHiLoad = new double[0];
    private volatile double predictedLoadKw = Double.NaN;
    private volatile double predictedPvKw   = Double.NaN;
    @Getter
    @Setter
    private volatile double pvProduction    = Double.NaN;
    private final DoubleAdder squaredErrorLoad   = new DoubleAdder();
    private final DoubleAdder squaredErrorPv     = new DoubleAdder();
    private final LongAdder    errorSamples      = new LongAdder();

    public double getTotalEnergyDemand() {
        return states.values().stream()
                .mapToDouble(TickDataMessage.AgentState::getDemand)
                .sum();
    }

    public double getTotalGreenEnergyGeneration() {
        return states.values().stream()
                .mapToDouble(TickDataMessage.AgentState::getProduction)
                .sum();
    }

    public double getTotalCnpNegotiations() {
        return states.values().stream()
                .mapToDouble(TickDataMessage.AgentState::getCnpNegotiations)
                .sum();
    }
    public void setForecast(double loadKw, double pvKw){
        this.predictedLoadKw = loadKw;
        this.predictedPvKw   = pvKw;
    }

    public double getPredLoad(){ return predictedLoadKw; }
    public double getPredPv()  { return predictedPvKw; }

    public double[] getForecastLoad(){ return latestForecastLoad; }
    public double[] getForecastPv  (){ return latestForecastPv;   }

    public void updateForecast(double[] fLoad, double[] fPv){
        this.latestForecastLoad = fLoad.clone();
        this.latestForecastPv   = fPv.clone();
    }

    public void addErrorSample(double errLoadKw, double errPvKw){
        squaredErrorLoad.add(errLoadKw*errLoadKw);
        squaredErrorPv.add(errPvKw  *errPvKw);
        errorSamples.increment();
    }

    public void resetErrorAccumulators() {
        squaredErrorLoad.reset();
        squaredErrorPv.reset();
        errorSamples.reset();
    }
    public double currentRmseLoad () { return Math.sqrt(
            squaredErrorLoad.sum()/Math.max(1,errorSamples.sum())); }
    public double currentRmsePv   () { return Math.sqrt(
            squaredErrorPv  .sum()/Math.max(1,errorSamples.sum())); }

    public void setFanChart(double[] loLoad, double[] hiLoad, double[] loPv, double[] hiPv){
        fanLoLoad = loLoad;
        fanHiLoad = hiLoad;
        fanLoPv = loPv;
        fanHiPv = hiPv;
    }
}
