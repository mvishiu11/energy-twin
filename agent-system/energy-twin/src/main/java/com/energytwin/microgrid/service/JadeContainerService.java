package com.energytwin.microgrid.service;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.Specifier;
import jade.util.leap.ArrayList;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service to manage the JADE platform.
 *
 * <p>This service first creates a main container (for core services like DF, AMS, etc.), then
 * creates an agent container (non‑main) with TopicManagement enabled.
 *
 * <p>The stopContainer() method stops the agent container on demand (via /simulation/stop endpoint)
 */
@Service
public class JadeContainerService {

  private ContainerController mainContainer;
  private ContainerController agentContainer;
  private static final ExecutorService jadeExecutor = Executors.newCachedThreadPool();
  private static final Logger logger = LoggerFactory.getLogger(JadeContainerService.class);

  /**
   * Starts the JADE platform:
   *
   * <ul>
   *   <li>Creates a main container (if not already running) and stores it in mainContainer.
   *   <li>Creates an agent container with TopicManagementService enabled and stores it in
   *       agentContainer.
   * </ul>
   */
  public synchronized void startContainer() {
    Runtime runtime = Runtime.instance();

    // Create a main container if not already running.
    if (mainContainer == null) {
      Profile mainProfile = new ProfileImpl();
      mainProfile.setParameter(Profile.MAIN, "true");
      mainContainer = submitTask(() -> runtime.createMainContainer(mainProfile));
      logger.info("Main container created.");
    }

    // Create an agent container with TopicManagementService enabled via Specifier.
    Profile agentProfile = new ProfileImpl();
    ArrayList serviceList = new ArrayList();
    serviceList.add(createTopicManagementSpecifier());
    agentProfile.setSpecifiers(Profile.SERVICES, serviceList);
    agentContainer = submitTask(() -> runtime.createAgentContainer(agentProfile));

    // Check if agentContainer was created successfully.
    if (agentContainer == null) {
      throw new RuntimeException("Agent container not created. Mandatory services may be missing.");
    }
    logger.info("Agent container created.");
  }

  private Specifier createTopicManagementSpecifier() {
    Specifier serviceSpec = new Specifier();
    serviceSpec.setClassName("jade.core.messaging.TopicManagementService");
    serviceSpec.setArgs(new Object[] {"true"});
    return serviceSpec;
  }

  private <T> T submitTask(Callable<T> task) {
    try {
      return jadeExecutor.submit(task).get();
    } catch (InterruptedException | ExecutionException e) {
      logger.error("Error during JADE container initialization", e);
      throw new RuntimeException("Error during JADE container initialization", e);
    }
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
      logger.info("Launched agent: {} ({})", agentName, agentClassName);
    } catch (StaleProxyException e) {
      logger.error("Error during JADE agent launch", e);
    }
  }

  /**
   * Stops the agent container gracefully.
   *
   * <p>This method is invoked by the /simulation/stop endpoint to stop the simulation agents.
   */
  public synchronized void stopContainer() {
    if (agentContainer != null) {
      try {
        agentContainer.kill();
        agentContainer = null;
        logger.info("Agent container successfully killed.");
      } catch (Exception e) {
        logger.error("Error stopping the agent container", e);
        throw new RuntimeException("Error stopping the agent container", e);
      }
    } else {
      logger.warn("Agent container is not running; nothing to stop.");
    }
  }
}
