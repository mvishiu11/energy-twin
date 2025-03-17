package com.energytwin.agents;

import com.energytwin.behaviours.ExampleBehaviour;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;

public class ExampleAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": is starting!");
        addBehaviour(new ExampleBehaviour(this));
    }
}