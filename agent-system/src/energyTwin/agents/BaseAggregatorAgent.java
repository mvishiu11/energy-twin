package energyTwin.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Map;

public class BaseAggregatorAgent extends Agent {
    // Store a simple hierarchy info: agentLocalName -> array of child localNames
    protected static Map<String, String[]> hierarchyMap = new HashMap<>();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " started. (BaseAggregatorAgent)");

        // Register in the Directory Facilitator (DF)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("EnergyAgent");
        sd.setName(getLocalName() + "-service");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // By default, add a behaviour to respond to "RequestHierarchy" messages
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg != null) {
                    String content = msg.getContent();
                    if ("RequestHierarchy".equals(content)) {
                        // Respond with the known hierarchy in some textual form (e.g. JSON or key=value)
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(serializeHierarchy(hierarchyMap));
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
        // Deregister from DF
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println(getLocalName() + " shutting down.");
    }

    /**
     * Called by child agents to provide their data.
     * By default, returns a placeholder; child classes can override.
     */
    public String gatherData() {
        return "Base Aggregator Data (override in child)";
    }

    /**
     * Add a relationship to the hierarchy map, e.g. aggregator -> child agents
     */
    protected void addChildRelation(String parent, String child) {
        String[] existing = hierarchyMap.getOrDefault(parent, new String[0]);
        String[] newArr = new String[existing.length + 1];
        System.arraycopy(existing, 0, newArr, 0, existing.length);
        newArr[newArr.length - 1] = child;
        hierarchyMap.put(parent, newArr);
    }

    /**
     * Minimal text-based serialization of the hierarchy map for demonstration.
     * In a real system, you might prefer JSON using Jackson or Gson.
     */
    private String serializeHierarchy(Map<String, String[]> map) {
        // Example format:
        // Aggregator=[Building1, Solar1]\n
        // Building1=[...]\n
        // ...
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            sb.append(entry.getKey())
                    .append("=")
                    .append(java.util.Arrays.toString(entry.getValue()))
                    .append("\n");
        }
        return sb.toString();
    }
}
