package com.energytwin.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Map;

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
