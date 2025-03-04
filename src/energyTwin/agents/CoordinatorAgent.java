package energyTwin.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class CoordinatorAgent extends Agent {

    // Adjust this to the name of your SolarAgent
    private static final String SOLAR_AGENT_NAME = "solar";

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " started.");

        // Add a TickerBehaviour to periodically request data from the SolarAgent
        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                // If you're running on the same container, the local name might be "solar"
                // If not, it could be "solar@<container>" or "solar@<platform>"
                request.addReceiver(getAID(SOLAR_AGENT_NAME));
                request.setContent("RequestGeneration");
                myAgent.send(request);

                // Wait for the response
                ACLMessage response = myAgent.receive();
                if (response != null) {
                    if (response.getPerformative() == ACLMessage.INFORM) {
                        String content = response.getContent();
                        System.out.println("Coordinator received solar output: " + content + " kW");
                        // Here, you can implement logic to use this generation data.
                    }
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " is shutting down.");
    }
}
