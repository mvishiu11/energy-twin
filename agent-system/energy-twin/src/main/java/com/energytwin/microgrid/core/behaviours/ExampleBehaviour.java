package com.energytwin.microgrid.core.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class ExampleBehaviour extends OneShotBehaviour {
    private final Agent agent;

    public ExampleBehaviour(Agent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    public void action() {
        System.out.println(this.getBehaviourName() + ": Behaviour started on agent: " + agent.getLocalName());
    }
}
