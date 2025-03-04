package energyTwin.main;

import energyTwin.agents.TopLevelAggregator;
import energyTwin.agents.BuildingAgent;
import energyTwin.agents.SolarAgent;
import energyTwin.gui.EnergySystemGUI;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class MainContainer {
    public static void main(String[] args) {
        // 1) Create JADE runtime
        Runtime rt = Runtime.instance();

        // 2) Create a default profile
        Profile profile = new ProfileImpl();
        // If you still want Jade's RMA console for debugging, uncomment:
        profile.setParameter(Profile.GUI, "true");

        // 3) Create the main container
        ContainerController mainContainer = rt.createMainContainer(profile);

        try {
            // 4) Create a top-level aggregator
            // We'll specify parent = "" (meaning no parent)
            Object[] aggregatorArgs = new Object[]{ "" };
            AgentController aggregator = mainContainer.createNewAgent(
                    "CampusAggregator",
                    TopLevelAggregator.class.getName(),
                    aggregatorArgs
            );
            aggregator.start();

            // 5) Create a building agent
            // Let's give the aggregator as a parent.
            Object[] buildingArgs = new Object[]{ "CampusAggregator" };
            AgentController building1 = mainContainer.createNewAgent(
                    "Building1",
                    BuildingAgent.class.getName(),
                    buildingArgs
            );
            building1.start();

            // 6) Create a solar agent
            // Also child of aggregator for simplicity
            Object[] solarArgs = new Object[]{ "CampusAggregator" };
            AgentController solar1 = mainContainer.createNewAgent(
                    "Solar1",
                    SolarAgent.class.getName(),
                    solarArgs
            );
            solar1.start();

            // 7) Launch our custom Swing GUI
            //    (Wait a little so the above agents register with the DF)
            Thread.sleep(2000);
            new EnergySystemGUI(mainContainer).setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
