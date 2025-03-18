package com.energytwin.microgrid.core.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 * A custom TickerBehaviour that calls an abstract onTick method.
 * Agents can extend this to implement their tick-specific logic.
 */
public abstract class TickBehaviour extends TickerBehaviour {

    /**
     * Constructor.
     * @param a the agent using this behaviour.
     * @param period the tick interval in milliseconds.
     */
    public TickBehaviour(Agent a, long period) {
        super(a, period);
    }

    /**
     * This method is called at every tick.
     * @param simulationTime the current simulation time, if applicable.
     */
    public abstract void onTick(long simulationTime);

    @Override
    protected void onTick() {
        onTick(System.currentTimeMillis());
    }
}
