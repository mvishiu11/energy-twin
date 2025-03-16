package energyTwin.gui;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Iterator;

import java.util.HashMap;
import java.util.Map;

/**
 * A short-lived agent that:
 *  1) Queries the DF to build the agent->parent map (hierarchy).
 *  2) Detects the top-level aggregator (where parent="").
 *  3) Requests "RequestAllData" from that aggregator to get each agent's live data.
 *  4) Passes the results back to the GUI, then self-destructs.
 */
public class GuiAgent extends Agent {
    private EnergySystemGUI gui;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof EnergySystemGUI) {
            gui = (EnergySystemGUI) args[0];
        }

        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                // 1) Query the DF to find all "EnergyAgent" services (build parentMap).
                Map<String, String> parentMap = new HashMap<>();
                try {
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("EnergyAgent");
                    template.addServices(sd);

                    DFAgentDescription[] results = DFService.search(myAgent, template);
                    for (DFAgentDescription res : results) {
                        String agentName = res.getName().getLocalName();

                        // Use JADE's leap Iterator to get the services
                        Iterator serviceIt = res.getAllServices();
                        while (serviceIt.hasNext()) {
                            ServiceDescription serv = (ServiceDescription) serviceIt.next();
                            // Now get all properties:
                            Iterator propIt = serv.getAllProperties();
                            while (propIt.hasNext()) {
                                Property prop = (Property) propIt.next();
                                if ("parent".equals(prop.getName())) {
                                    String parentVal = (String) prop.getValue();
                                    parentMap.put(agentName, parentVal);
                                }
                            }
                        }
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }

                // 2) Find a top-level aggregator name (parent = "")
                //    If multiple top-level agents exist, pick the first encountered.
                String aggregatorName = null;
                for (Map.Entry<String, String> entry : parentMap.entrySet()) {
                    if (entry.getValue() == null || entry.getValue().isEmpty()) {
                        aggregatorName = entry.getKey();
                        break;
                    }
                }

                // 3) If we found an aggregator, request "RequestAllData" from it.
                Map<String, String> dataMap = new HashMap<>(); // agentName -> current data
                if (aggregatorName != null) {
                    ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                    req.addReceiver(getAID(aggregatorName));
                    req.setContent("RequestAllData");
                    myAgent.send(req);

                    // Wait for the aggregator to reply with data
                    ACLMessage reply = blockingReceive(MessageTemplate.MatchSender(getAID(aggregatorName)));
                    if (reply != null && reply.getPerformative() == ACLMessage.INFORM) {
                        String serializedData = reply.getContent();
                        dataMap = parseDataString(serializedData);
                    }
                }

                // 4) Update the GUI with both hierarchy + data
                if (gui != null) {
                    gui.updateHierarchyAndData(parentMap, dataMap);
                }

                // 5) Terminate this short-lived agent
                myAgent.doDelete();
            }
        });
    }

    /**
     * Parse lines like:
     *   Building1=BuildingLoad=57kW
     *   Solar1=SolarGen=12kW
     * into a map { "Building1" -> "BuildingLoad=57kW", "Solar1" -> "SolarGen=12kW" }
     */
    private Map<String, String> parseDataString(String text) {
        Map<String, String> dataMap = new HashMap<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                int eq = line.indexOf('=');
                if (eq != -1) {
                    String agentName = line.substring(0, eq);
                    String val = line.substring(eq + 1);
                    dataMap.put(agentName, val);
                }
            }
        }
        return dataMap;
    }
}