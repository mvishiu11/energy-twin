package com.energytwin.microgrid.agentfusion;

import com.energytwin.microgrid.service.SimulationConfigService;
import com.energytwin.microgrid.service.SimulationControlService;
import jade.core.Agent;
import org.springframework.context.ApplicationContext;
import com.energytwin.microgrid.service.LogAggregatorService;
import com.energytwin.microgrid.agentfusion.util.SpringContext;

/**
 * Base JADE agent class that integrates with Spring.
 * It initializes Spring dependencies via Template Method pattern
 * and provides a method to add custom setup config.
 */
public abstract class SpringAgent extends Agent {

    protected LogAggregatorService logService;
    protected SimulationConfigService simulationConfigService;
    protected SimulationControlService simulationControlService;

    @Override
    protected final void setup() {
        initSpring();
        onAgentSetup();
    }

    /**
     * Retrieves Spring dependencies by obtaining the application context
     * and autowiring this agent.
     */
    protected void initSpring() {
        ApplicationContext ctx = SpringContext.getApplicationContext();
        if (ctx != null) {
            logService = ctx.getBean(LogAggregatorService.class);
            simulationConfigService = ctx.getBean(SimulationConfigService.class);
            simulationControlService = ctx.getBean(SimulationControlService.class);
        } else {
            System.err.println("Spring ApplicationContext is not initialized!");
        }
    }

    /**
     * Logs a message using the Spring-managed LogAggregatorService.
     * @param message the message to log
     */
    public void log(String message) {
        logService.log(getLocalName() + " - " + message);
        System.out.println(getLocalName() + " - " + message);
    }

    /**
     * Hook method for child classes to perform custom setup actions.
     */
    protected abstract void onAgentSetup();

    public abstract void onTick(long simulationTime);
}
