package com.energytwin.microgrid.core.behaviours.tick;

import com.energytwin.microgrid.agentfusion.SpringAgent;
import com.energytwin.microgrid.service.SimulationControlService;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.Getter;

/**
 * TickBroadcastBehaviour is responsible for broadcasting simulation tick messages.
 * It checks if the simulation is paused and increments simulation time.
 * It dynamically adjusts its tick interval by using the reset() method when the configuration changes.
 */
public class TickBroadcastBehaviour extends TickerBehaviour {

  @Getter
  private long simulationTime = 0;
  private final AID tickTopic;
  private final SimulationControlService simulationControlService;
  private final SpringAgent agent;
  private long currentPeriod;

  /**
   * Constructor.
   *
   * @param a the agent that will run this behaviour.
   * @param period the initial tick interval in milliseconds.
   * @param tickTopic the AID of the tick topic to which tick messages will be sent.
   * @param simulationControlService the simulation control service for getting tick increments and pause status.
   */
  public TickBroadcastBehaviour(SpringAgent a, long period, AID tickTopic, SimulationControlService simulationControlService) {
    super(a, period);
    this.agent = a;
    this.tickTopic = tickTopic;
    this.simulationControlService = simulationControlService;
    this.currentPeriod = period;
  }

  @Override
  protected void onTick() {
    long newPeriod = simulationControlService.getSimulationDelay();
    if (newPeriod != currentPeriod) {
      currentPeriod = newPeriod;
      reset(newPeriod);
      agent.log("Tick period updated to: {}", newPeriod);
    }

    if (simulationControlService.isPaused()) {
      return;
    }

    simulationTime += 1;
    ACLMessage tickMsg = new ACLMessage(ACLMessage.INFORM);
    tickMsg.setOntology("TICK");
    tickMsg.setContent(String.valueOf(simulationTime));
    tickMsg.addReceiver(tickTopic);
    myAgent.send(tickMsg);
    agent.log("Broadcast tick: {}", simulationTime);
  }
}
