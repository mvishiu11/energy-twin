package com.energytwin.microgrid.core.aggregator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Stores static device meta sent once at setup. */
public final class AggregatorMetaStore {

    public record BatteryMeta(double capacity, double etaC, double etaD,
                              double cRate){}

    private final Map<String,BatteryMeta> batteries = new ConcurrentHashMap<>();

    public void addBattery(String name, BatteryMeta m){ batteries.put(name, m); }
    public BatteryMeta getBattery(String name){ return batteries.get(name); }
    public Map<String,BatteryMeta> allBatteries(){ return batteries; }
}
