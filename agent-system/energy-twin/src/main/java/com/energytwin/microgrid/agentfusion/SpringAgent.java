package com.energytwin.microgrid.agentfusion;

import jade.core.Agent;
import org.springframework.context.ApplicationContext;
import com.energytwin.microgrid.service.LogAggregatorService;
import com.energytwin.microgrid.agentfusion.util.SpringContext;

/**
 * Base JADE agent class that integrates with Spring.
 * It provides a method to initialize Spring dependencies.
 */
public abstract class SpringAgent extends Agent {

    protected LogAggregatorService logService;

    protected void setup() {
        initSpring();
    }

    /**
     * Retrieves Spring dependencies by obtaining the application context
     * and autowiring this agent.
     */
    protected void initSpring() {
        ApplicationContext ctx = SpringContext.getApplicationContext();
        if (ctx != null) {
            logService = ctx.getBean(LogAggregatorService.class);
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

    public abstract void onTick(long simulationTime);
}
