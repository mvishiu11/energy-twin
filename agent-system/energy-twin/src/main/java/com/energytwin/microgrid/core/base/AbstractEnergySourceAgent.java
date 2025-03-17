package com.energytwin.microgrid.core.base;

/**
 * Abstract base class for energy source agents.
 * Includes common properties and methods for agents that produce energy.
 */
public abstract class AbstractEnergySourceAgent extends AbstractSimAgent {
    protected double productionRate; // Energy produced per tick

    /**
     * Sets the production rate for this energy source.
     * @param rate production rate value
     */
    public void setProductionRate(double rate) {
        this.productionRate = rate;
    }
}
