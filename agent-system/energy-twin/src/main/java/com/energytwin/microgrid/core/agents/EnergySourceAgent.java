package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.agentfusion.SpringAgent;

public class EnergySourceAgent extends SpringAgent {

    @Override
    protected void setup() {
        // Initialize Spring dependencies
        initSpring();
        log("Energy Source Agent started.");

        // Simulation logic – for demonstration:
        for (int i = 1; i <= 5; i++) {
            log("Produced " + (100 + i * 10) + " kW at timestep " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log("Interrupted during simulation.");
            }
        }

        log("Energy Source Agent finished.");
        doDelete(); // Terminate the agent
    }
}
