package com.energytwin;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final ExecutorService jadeExecutor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        final Runtime runtime = Runtime.instance();
        final Profile profile = new ProfileImpl();
        profile.setParameter("gui", "true");

        try {
            // Create Main Container
            final ContainerController mainContainer = jadeExecutor.submit(() -> runtime.createMainContainer(profile)).get();

            // Create and start MarketAgents
            final AgentController exampleAgent = mainContainer.createNewAgent(
                    "MarketAgent1",
                    "com.energytwin.agents.ExampleAgent",
                    null
            );
            exampleAgent.start();

            System.out.println("All agents started!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
