package com.energytwin.microgrid.core.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.energytwin.microgrid.core.base.AbstractSimAgent;

/**
 * Behaviour that subscribes to tick messages on a given topic.
 */
public class TickSubscriberBehaviour extends CyclicBehaviour {

    private final AbstractSimAgent simAgent;

    /**
     * Constructor.
     * @param agent the simulation agent.
     */
    public TickSubscriberBehaviour(AbstractSimAgent agent) {
        super(agent);
        this.simAgent = agent;
    }

    @Override
    public void action() {
        // Match messages with ontology "TICK"
        MessageTemplate mt = MessageTemplate.MatchOntology("TICK");
        ACLMessage msg = myAgent.receive(mt);
        if (msg != null) {
            try {
                long tickTime = Long.parseLong(msg.getContent());
                simAgent.onTick(tickTime);
            } catch (NumberFormatException e) {
                simAgent.log("Error parsing tick message: " + msg.getContent());
            }
        } else {
            block();
        }
    }
}
