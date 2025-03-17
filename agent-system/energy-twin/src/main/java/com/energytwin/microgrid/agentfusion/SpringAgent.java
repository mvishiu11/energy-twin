package com.energytwin.microgrid.agentfusion;

import jade.core.Agent;
import org.springframework.context.ApplicationContext;
import com.energytwin.microgrid.service.LogAggregatorService;
import com.energytwin.microgrid.agentfusion.util.SpringContext;

public abstract class SpringAgent extends Agent {

    protected LogAggregatorService logService;

    /**
     * Call this method at the beginning of the agent's setup method.
     */
    protected void initSpring() {
        ApplicationContext ctx = SpringContext.getApplicationContext();
        if (ctx != null) {
            // Explicitly retrieve the bean from the context
            logService = ctx.getBean(LogAggregatorService.class);
        } else {
            System.err.println("Spring ApplicationContext is not initialized!");
        }
    }

    protected void log(String message) {
        // Now logService should not be null if initSpring() succeeded.
        logService.log(getLocalName() + " - " + message);
    }
}
