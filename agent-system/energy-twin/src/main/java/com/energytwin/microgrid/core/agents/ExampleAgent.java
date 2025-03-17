package com.energytwin.microgrid.core.agents;

import com.energytwin.microgrid.core.behaviours.ExampleBehaviour;
import jade.core.Agent;

public class ExampleAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": is starting!");
        addBehaviour(new ExampleBehaviour(this));
    }
}