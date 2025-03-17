package com.energytwin.microgrid.service;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service to manage the JADE platform.
 * First, it creates a main container (for DF, AMS, etc.),
 * then creates a separate agent container with TopicManagement enabled.
 */
@Service
public class JadeContainerService {

    private ContainerController agentContainer;
    private static final ExecutorService jadeExecutor = Executors.newCachedThreadPool();

    /**
     * Starts the JADE platform:
     * - Creates a main container (if not already running).
     * - Creates an agent container (non‑main) with TopicManagement enabled.
     *
     * @throws ExecutionException if container creation fails
     * @throws InterruptedException if container creation is interrupted
     */
    public synchronized void startContainer() throws ExecutionException, InterruptedException {
        Runtime runtime = Runtime.instance();

        // Check if a main container is already running; if not, create one.
        Profile mainProfile = new ProfileImpl();
        mainProfile.setParameter(Profile.MAIN, "true");
        // Optionally, you can set additional parameters for the main container.
        jadeExecutor.submit(() -> runtime.createMainContainer(mainProfile)).get();

        // Now create an agent container with TopicManagement enabled.
        Profile agentProfile = new ProfileImpl();
        // Enable TopicManagement for this container.
        agentProfile.setParameter("jade.core.messaging.TopicManagement", "true");
        agentProfile.setParameter("jade.core.services", "jade.core.messaging.TopicManagement");
        // Create the agent container.
        agentContainer = jadeExecutor.submit(() -> runtime.createAgentContainer(agentProfile)).get();
    }

    /**
     * Launches a new JADE agent in the agent container with the given name and class.
     *
     * @param agentName the name of the agent.
     * @param agentClassName the fully qualified class name of the agent.
     */
    public void launchAgent(String agentName, String agentClassName) {
        try {
            Object[] args = new Object[] {};
            AgentController agent = agentContainer.createNewAgent(agentName, agentClassName, args);
            agent.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
