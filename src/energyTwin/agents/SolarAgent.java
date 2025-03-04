package energyTwin.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import jade.domain.FIPAAgentManagement.*;
import jade.domain.DFService;
import jade.domain.FIPAException;

import java.util.Random;

public class SolarAgent extends Agent {
    private String parentName;
    private Random rand = new Random();

    @Override
    protected void setup() {
        // 1) Read parent from arguments
        Object[] args = getArguments();
        parentName = (args != null && args.length > 0) ? (String) args[0] : "";

        System.out.println(getLocalName() + " started. Parent=" + parentName);

        // 2) Register in DF
        registerAgentInDF(parentName);

        // 3) Respond to "ProvideYourData" with random generation
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST &&
                            "ProvideYourData".equals(msg.getContent())) {
                        int generation = rand.nextInt(50);
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("SolarGen=" + generation + "kW");
                        myAgent.send(reply);
                    }
                } else {
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        deregisterAgentFromDF();
        System.out.println(getLocalName() + " shutting down.");
    }

    private void registerAgentInDF(String parent) {
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

    private void deregisterAgentFromDF() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
