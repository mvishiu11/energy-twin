package energyTwin.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.util.leap.Iterator;

import java.util.Random;

public class BuildingAgent extends Agent {
    private Random rand = new Random();

    @Override
    protected void setup() {
        // Read parent from arguments, register in DF
        Object[] args = getArguments();
        String parentName = (args != null && args.length > 0) ? (String) args[0] : "";
        registerInDF(parentName);

        // Respond to "ProvideYourData"
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg != null) {
                    if ("ProvideYourData".equals(msg.getContent())) {
                        // Return a random building load
                        int load = rand.nextInt(100);
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("BuildingLoad=" + load + "kW");
                        send(reply);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void registerInDF(String parent) {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());

            ServiceDescription sd = new ServiceDescription();
            sd.setType("EnergyAgent");
            sd.setName(getLocalName() + "-service");
            sd.addProperties(new Property("parent", parent));
            dfd.addServices(sd);

            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
