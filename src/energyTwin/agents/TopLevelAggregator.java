package energyTwin.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Iterator;

import java.util.HashMap;
import java.util.Map;

/**
 * A "top-level" aggregator.
 *  - parentName = "" => registers itself in the DF with no parent.
 *  - Periodically requests "ProvideYourData" from all agents (for demo, we broadcast).
 *  - Collects INFORM replies and stores them in dataMap.
 *  - On "RequestAllData", returns all data in a multiline string.
 */
public class TopLevelAggregator extends Agent {
    private final Map<String, String> dataMap = new HashMap<>(); // agentName -> data

    @Override
    protected void setup() {
        // 1) Read the "parent" argument
        Object[] args = getArguments();
        String parentName = (args != null && args.length > 0) ? (String) args[0] : "";

        System.out.println(getLocalName() + " started. Parent=" + parentName);

        // 2) Register in the DF with "parent" property
        registerInDF(parentName);

        // 3) Periodically request data from child agents
        addBehaviour(new TickerBehaviour(this, 3000) {
            @Override
            protected void onTick() {
                //  An AMS broadcast for a quick demo
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setContent("ProvideYourData");
                msg.addReceiver(getAMS());
                send(msg);

                // After sending, let's handle any INFORM replies we receive "right now".
                ACLMessage reply;
                while ((reply = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM))) != null) {
                    String senderName = reply.getSender().getLocalName();
                    String data = reply.getContent();
                    dataMap.put(senderName, data);
                    System.out.println(getLocalName() + " stored data from " + senderName + ": " + data);
                }
            }
        });

        // 4) Respond to "RequestAllData" with the entire dataMap
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage req = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (req != null) {
                    if ("RequestAllData".equals(req.getContent())) {
                        // Build a multiline string: e.g. "Building1=BuildingLoad=60kW\nSolar1=SolarGen=20kW\n"
                        StringBuilder sb = new StringBuilder();
                        for (Map.Entry<String, String> e : dataMap.entrySet()) {
                            sb.append(e.getKey()).append("=").append(e.getValue()).append("\n");
                        }
                        ACLMessage reply = req.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(sb.toString());
                        send(reply);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void registerInDF(String parent) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("EnergyAgent");
        sd.setName(getLocalName() + "-service");
        // If aggregator is top-level, parent == "".
        sd.addProperties(new Property("parent", parent));

        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println(getLocalName() + " shutting down.");
    }
}
