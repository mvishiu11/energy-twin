package com.energytwin.microgrid.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

/**
 * Manages simulation control parameters.
 */
@Getter
@Service
public class SimulationControlService {

    @Setter
    private long tickIntervalMillis = 1000; // Default; can be overridden by YAML config
    @Setter
    private int speedUpFactor = 3600;       // Default speed-up factor
    private volatile boolean paused = false;

    /**
     * Returns the simulation tick increment in simulation seconds.
     * @return simulation tick increment.
     */
    public long getSimulationTickIncrement() {
        return tickIntervalMillis * speedUpFactor;
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }
}