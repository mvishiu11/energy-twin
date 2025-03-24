package com.energytwin.microgrid.core.behaviours;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import com.energytwin.microgrid.service.SimulationControlService;
import com.energytwin.microgrid.agentfusion.SpringAgent;
import lombok.Getter;

/**
 * TickBroadcastBehaviour is responsible for broadcasting simulation tick messages.
 * It checks if the simulation is paused and then increments the simulation time based on
 * the configured speed factor. The tick message is sent to the provided tick topic.
 */
public class TickBroadcastBehaviour extends TickerBehaviour {

    /**
     * -- GETTER --
     *  Returns the current simulation time.
     */
    @Getter
    private long simulationTime = 0;
    private final AID tickTopic;
    private final SimulationControlService simulationControlService;
    private final SpringAgent agent;

    /**
     * Constructor.
     *
     * @param a the agent that will run this behavior.
     * @param period the tick interval in milliseconds.
     * @param tickTopic the AID of the tick topic to which tick messages will be sent.
     * @param simulationControlService the simulation control service for getting tick increments and pause status.
     */
    public TickBroadcastBehaviour(SpringAgent a, long period, AID tickTopic, SimulationControlService simulationControlService) {
        super(a, period);
        this.agent = a;
        this.tickTopic = tickTopic;
        this.simulationControlService = simulationControlService;
    }

    @Override
    protected void onTick() {
        if (simulationControlService.isPaused()) {
            return;
        }
        simulationTime += simulationControlService.getSimulationTickIncrement();
        ACLMessage tickMsg = new ACLMessage(ACLMessage.INFORM);
        tickMsg.setOntology("TICK");
        tickMsg.setContent(String.valueOf(simulationTime));
        tickMsg.addReceiver(tickTopic);
        myAgent.send(tickMsg);
        this.agent.log("Broadcasted tick: {}", simulationTime);
    }

}
