package com.energytwin.microgrid.agentfusion;

import com.energytwin.microgrid.agentfusion.util.SpringContext;
import com.energytwin.microgrid.registry.AgentStateRegistry;
import com.energytwin.microgrid.service.EventControlService;
import com.energytwin.microgrid.service.LogAggregatorService;
import com.energytwin.microgrid.service.SimulationConfigService;
import com.energytwin.microgrid.service.SimulationControlService;
import com.energytwin.microgrid.ws.dto.TickDataMessage;
import com.energytwin.microgrid.ws.simulation.SimulationControlServiceWS;
import jade.core.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.ApplicationContext;

/**
 * Base JADE agent class that integrates with Spring. It initializes Spring dependencies via
 * Template Method pattern and provides a method to add custom setup config.
 */
public abstract class SpringAgent extends Agent {

  protected LogAggregatorService logService;
  protected SimulationConfigService simulationConfigService;
  protected SimulationControlService simulationControlService;
  protected AgentStateRegistry registry;
  protected SimulationControlServiceWS simulationControlServiceWS;
  protected EventControlService eventControlService;
  private static final Logger logger = LoggerFactory.getLogger(SpringAgent.class);

  @Override
  protected final void setup() {
    initSpring();
    onAgentSetup();
  }

  /**
   * Retrieves Spring dependencies by obtaining the application context and autowiring this agent.
   */
  protected void initSpring() {
    ApplicationContext ctx = SpringContext.getApplicationContext();
    if (ctx != null) {
      logService = ctx.getBean(LogAggregatorService.class);
      simulationConfigService = ctx.getBean(SimulationConfigService.class);
      simulationControlService = ctx.getBean(SimulationControlService.class);
      registry = ctx.getBean(AgentStateRegistry.class);
      simulationControlServiceWS = ctx.getBean(SimulationControlServiceWS.class);
      eventControlService = ctx.getBean(EventControlService.class);
    } else {
      System.err.println("Spring ApplicationContext is not initialized!");
    }
  }

  /**
   * Logs a message using both the Spring-managed LogAggregatorService (if available) and the SLF4J
   * logger.
   *
   * <p>This method supports parameterized messages. If the last argument is a Throwable, it is
   * logged as an error.
   *
   * @param message the message to log (with SLF4J formatting placeholders).
   * @param args optional arguments; if the last one is a Throwable, it is treated as an error.
   */
  public void log(String message, Object... args) {
    String prefixedMessage = getLocalName() + " - " + message;

    FormattingTuple ft = MessageFormatter.arrayFormat(prefixedMessage, args);
    String formattedMessage = ft.getMessage();
    Throwable throwable = ft.getThrowable();

    logger.info(formattedMessage);

    if (throwable != null) {
      logger.error(formattedMessage, throwable);
    }

    if (logService != null) {
      logService.log(getLocalName(), formattedMessage);
    } else {
      logger.error("LogAggregatorService is not initialized!");
    }
  }

  /** Hook method for child classes to perform custom setup actions. */
  protected abstract void onAgentSetup();
  protected void reportState(double demand, double production, double soc, boolean isBroken){
    TickDataMessage.AgentState st = new TickDataMessage.AgentState();
    st.setDemand(demand);
    st.setProduction(production);
    st.setStateOfCharge(soc);
    st.setBroken(isBroken);
    registry.update(getLocalName(), st);
  }

  public void reportState(double demand, double production, double soc){
    TickDataMessage.AgentState st = new TickDataMessage.AgentState();
    st.setDemand(demand);
    st.setProduction(production);
    st.setStateOfCharge(soc);
    registry.update(getLocalName(), st);
  }
}
