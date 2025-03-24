package com.energytwin.microgrid.core.behaviours;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.energytwin.microgrid.core.base.AbstractSimAgent;

/**
 * TickSubscriberBehaviour subscribes the agent to a given tick topic and then listens for "TICK" messages.
 */
public class TickSubscriberBehaviour extends CyclicBehaviour {

    private final AbstractSimAgent simAgent;
    private final AID tickTopic;

    /**
     * Constructs a new TickSubscriberBehaviour.
     * Registers the agent as a subscriber to the specified tick topic.
     *
     * @param simAgent the simulation agent.
     * @param tickTopic the AID of the tick topic (e.g. "TickTopic").
     */
    public TickSubscriberBehaviour(AbstractSimAgent simAgent, AID tickTopic) {
        super(simAgent);
        this.simAgent = simAgent;
        this.tickTopic = tickTopic;
        try {
            TopicManagementHelper topicHelper = (TopicManagementHelper) simAgent.getHelper(TopicManagementHelper.SERVICE_NAME);
            topicHelper.register(this.tickTopic);
            simAgent.log("Successfully subscribed to tick topic: " + this.tickTopic.getLocalName());
        } catch (Exception e) {
            simAgent.log("Error subscribing to tick topic: {}", e.getMessage(), e);
        }
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchOntology("TICK");
        ACLMessage msg = myAgent.receive(mt);
        if (msg != null) {
            try {
                long tickTime = Long.parseLong(msg.getContent());
                simAgent.onTick(tickTime);
            } catch (NumberFormatException e) {
                simAgent.log("Error parsing tick message: {}", msg.getContent(), e);
            }
        } else {
            block();
        }
    }
}
