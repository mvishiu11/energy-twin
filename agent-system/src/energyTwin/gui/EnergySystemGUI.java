package energyTwin.gui;

import jade.wrapper.ContainerController;
import jade.wrapper.AgentController;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.HashMap;

public class EnergySystemGUI extends JFrame {
    private ContainerController container;

    // Left side: JTree
    private JTree agentTree;
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;

    // Right side: a text area show selected agent data
    private JTextArea dataArea;

    // We store the results from aggregator: agentName -> "BuildingLoad=NNkW"
    private Map<String, String> dataMap = new HashMap<>();
    // Also store the parentMap from DF for building the tree
    private Map<String, String> parentMap = new HashMap<>();

    public EnergySystemGUI(ContainerController container) {
        super("Energy System Agent Hierarchy & Data");
        this.container = container;

        // Prepare the JTree
        rootNode = new DefaultMutableTreeNode("Root"); // We'll fill dynamically
        treeModel = new DefaultTreeModel(rootNode);
        agentTree = new JTree(treeModel);
        agentTree.setCellRenderer(new AgentTreeCellRenderer());  // custom icons

        // Listen for selection
        agentTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode =
                        (DefaultMutableTreeNode) agentTree.getLastSelectedPathComponent();
                if (selectedNode == null) return;

                String nodeLabel = selectedNode.toString();
                // Because we appended data in parentheses, or we might just store raw name
                // If your node name is e.g. "Building1", that is the agent's local name
                // Let's show the data for that agent if we have it
                String agentName = nodeLabel; // simple approach

                String agentData = dataMap.getOrDefault(agentName, "No data available");
                dataArea.setText("Agent: " + agentName + "\n" + "Data: " + agentData);
            }
        });

        JScrollPane treeScroll = new JScrollPane(agentTree);

        // Prepare the data area on the right
        dataArea = new JTextArea();
        dataArea.setEditable(false);
        dataArea.setBorder(BorderFactory.createTitledBorder("Agent Data"));
        JScrollPane dataScroll = new JScrollPane(dataArea);

        // Combine them in a JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, dataScroll);
        splitPane.setDividerLocation(250);

        // Create a top panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshBtn = new JButton("Refresh Hierarchy & Data");
        refreshBtn.addActionListener((ActionEvent e) -> fetchAndUpdateHierarchyAndData());
        buttonPanel.add(refreshBtn);

        JButton addAgentBtn = new JButton("Add Agent");
        addAgentBtn.addActionListener((ActionEvent e) -> showAddAgentDialog());
        buttonPanel.add(addAgentBtn);

        // Layout main frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buttonPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Start a timer to refresh every 5s automatically
        Timer timer = new Timer(5000, e -> fetchAndUpdateHierarchyAndData());
        timer.start();
    }

    /**
     * Creates a short-lived GuiAgent to query:
     *  1) DF for parentMap
     *  2) aggregator for dataMap
     */
    private void fetchAndUpdateHierarchyAndData() {
        try {
            String agentName = "GuiAgent_" + System.currentTimeMillis();
            Object[] args = new Object[]{ this }; // pass this GUI reference
            AgentController guiAgent = container.createNewAgent(agentName, "energyTwin.gui.GuiAgent", args);
            guiAgent.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Called by GuiAgent once it parses the agent->parent map and the aggregator dataMap.
     */
    public void updateHierarchyAndData(Map<String, String> newParentMap, Map<String, String> newDataMap) {
        this.parentMap = newParentMap;
        this.dataMap = newDataMap;

        // Rebuild the tree
        rootNode.removeAllChildren();

        // 1) Find top-level agents (parent = "" or null)
        for (Map.Entry<String, String> entry : parentMap.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                String agentName = entry.getKey();
                DefaultMutableTreeNode topNode = new DefaultMutableTreeNode(agentName);
                rootNode.add(topNode);
                buildChildren(topNode, agentName);
            }
        }
        treeModel.reload();
        dataArea.setText(""); // Clear data area
    }

    private void buildChildren(DefaultMutableTreeNode parentNode, String parentAgent) {
        for (Map.Entry<String, String> e : parentMap.entrySet()) {
            String childName = e.getKey();
            String childParent = e.getValue();
            if (parentAgent.equals(childParent)) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childName);
                parentNode.add(childNode);
                buildChildren(childNode, childName);
            }
        }
    }

    /**
     * UI dialog to create a new agent dynamically.
     */
    private void showAddAgentDialog() {
        JTextField nameField = new JTextField(10);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"BuildingAgent", "SolarAgent"});
        JTextField parentField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Agent Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Agent Type:"));
        panel.add(typeBox);
        panel.add(new JLabel("Parent (name):"));
        panel.add(parentField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create New Agent",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String localName = nameField.getText().trim();
            String agentType = (String) typeBox.getSelectedItem();
            String parent = parentField.getText().trim();
            if (localName.isEmpty()) return;

            createNewAgent(localName, "energyTwin.agents." + agentType, parent);
        }
    }

    private void createNewAgent(String localName, String className, String parentName) {
        try {
            Object[] args = new Object[]{ parentName };
            AgentController ac = container.createNewAgent(localName, className, args);
            ac.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------
    // Custom cell renderer for the JTree (to show icons, etc.)
    // -------------------------------------------------------------------
    private class AgentTreeCellRenderer extends DefaultTreeCellRenderer {
        private Icon buildingIcon;
        private Icon solarIcon;
        private Icon aggregatorIcon;

        public AgentTreeCellRenderer() {
            // Can load actual image files from resources
            buildingIcon = UIManager.getIcon("FileView.directoryIcon");       // placeholder
            solarIcon = UIManager.getIcon("FileView.fileIcon");               // placeholder
            aggregatorIcon = UIManager.getIcon("OptionPane.informationIcon"); // placeholder
        }

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            // 'value' is typically a DefaultMutableTreeNode
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            String nodeStr = node.toString();

            // Heuristic to decide icon: if agent name contains "Building", "Solar", or else aggregator
            String lower = nodeStr.toLowerCase();
            if (lower.contains("building")) {
                setIcon(buildingIcon);
            } else if (lower.contains("solar")) {
                setIcon(solarIcon);
            } else {
                // Could be aggregator or anything else
                setIcon(aggregatorIcon);
            }

            return this;
        }
    }
}
