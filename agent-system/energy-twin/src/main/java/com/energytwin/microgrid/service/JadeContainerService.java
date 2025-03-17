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

@Service
public class JadeContainerService {

    private ContainerController container;
    private static final ExecutorService jadeExecutor = Executors.newCachedThreadPool();

    public synchronized void startContainer() throws ExecutionException, InterruptedException {
        if (container == null) {
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN, "false");
            // profile.setParameter("gui", "true");
            Runtime runtime = Runtime.instance();
            container = jadeExecutor.submit(() -> runtime.createMainContainer(profile)).get();
        }
    }

    public void launchAgent(String agentName, String agentClassName) {
        try {
            Object[] args = new Object[] {};
            AgentController agent = container.createNewAgent(agentName, agentClassName, args);
            agent.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
