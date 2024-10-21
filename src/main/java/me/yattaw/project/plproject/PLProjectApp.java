package me.yattaw.project.plproject;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class PLProjectApp {

    private JTabbedPane tabbedPane;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PLProjectApp::new);
    }

    public PLProjectApp() {
        // Set FlatLaf look and feel (optional)
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the main frame
        JFrame frame = new JFrame("PLProject Obfuscator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(new GridLayout(1, 2));  // Split the layout into two equal panels

        // Left panel with buttons and class list
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        // Buttons for uploading jar and showing classes
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1));
        JButton uploadJarButton = new JButton("Upload jar");
        buttonPanel.add(uploadJarButton);

        // JTree to display class/method/field structure
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Classes");
        JTree classTree = new JTree(root);
        JScrollPane classScrollPane = new JScrollPane(classTree);

        leftPanel.add(buttonPanel, BorderLayout.NORTH);
        leftPanel.add(classScrollPane, BorderLayout.CENTER);

        // Right panel to show decompiled instructions with closable tabs
        tabbedPane = new JTabbedPane();
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        rightPanel.add(tabbedPane, BorderLayout.CENTER);

        // Add the left and right panels to the frame
        frame.add(leftPanel);
        frame.add(rightPanel);

        // ActionListener for Upload Button (to load hardcoded values)
        uploadJarButton.addActionListener(e -> loadHardcodedClasses(root, classTree));

        // Listener for selecting a method/field in the tree
        classTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) classTree.getLastSelectedPathComponent();
                if (selectedNode == null) return;  // No selection

                // Check if the selected node is a method or field (leaf nodes)
                if (selectedNode.isLeaf()) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
                    if (parentNode != null && parentNode.getParent() != null) {
                        String className = parentNode.getParent().toString();
                        String itemName = selectedNode.toString();
                        String tabIdentifier = className + ": " + itemName;

                        // Check if a tab with this identifier is already open
                        if (!isTabAlreadyOpen(tabIdentifier)) {
                            createClosableTab(tabIdentifier);
                        }
                    }
                }
            }
        });

        // Show the frame
        frame.setVisible(true);
    }

    private void loadHardcodedClasses(DefaultMutableTreeNode root, JTree tree) {
        root.removeAllChildren();  // Clear previous data

        // Create a class node for "Main"
        DefaultMutableTreeNode mainClassNode = new DefaultMutableTreeNode("Main");
        root.add(mainClassNode);

        // Methods under the "Main" class
        DefaultMutableTreeNode methodsNode = new DefaultMutableTreeNode("Methods");
        mainClassNode.add(methodsNode);
        methodsNode.add(new DefaultMutableTreeNode("main()"));
        methodsNode.add(new DefaultMutableTreeNode("boolean isPrime(int num)"));

        // Fields under the "Main" class
        DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode("Fields");
        mainClassNode.add(fieldsNode);
        fieldsNode.add(new DefaultMutableTreeNode("public static int ONE_HUNDRED = 100"));

        // Update the JTree UI
        ((DefaultTreeModel) tree.getModel()).reload();
    }

    private void createClosableTab(String tabIdentifier) {
        JPanel tabPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel(tabIdentifier);
        tabPanel.add(infoLabel, BorderLayout.NORTH);

        // Create options for obfuscation settings
        JPanel obfuscationPanel = new JPanel();
        obfuscationPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Use FlowLayout to prevent components from expanding

        JCheckBox xorObfuscation = new JCheckBox("XOR Field Obfuscation");
        JCheckBox methodRemapper = new JCheckBox("Method Name Obfuscation");
        JCheckBox stringEncryption = new JCheckBox("String Encryption");

        // Create the JComboBox and set a preferred size
        JComboBox<String> nameRemapping = new JComboBox<>();
        nameRemapping.addItem("Random remapper");
        nameRemapping.addItem("Keyword remapper");
        nameRemapping.setPreferredSize(new Dimension(200, xorObfuscation.getPreferredSize().height)); // Set a similar size as checkboxes

        obfuscationPanel.add(xorObfuscation);
        obfuscationPanel.add(methodRemapper);
        obfuscationPanel.add(stringEncryption);
        obfuscationPanel.add(nameRemapping);

        tabPanel.add(obfuscationPanel, BorderLayout.CENTER);

        // Add the tab to the tabbed pane with closable functionality
        tabbedPane.addTab(tabIdentifier, tabPanel);
        int index = tabbedPane.indexOfComponent(tabPanel);
        tabbedPane.setTabComponentAt(index, new ClosableTabComponent(tabbedPane, tabIdentifier));
    }


    private boolean isTabAlreadyOpen(String tabIdentifier) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(tabIdentifier)) {
                tabbedPane.setSelectedIndex(i);  // Select the existing tab
                return true;
            }
        }
        return false;
    }

    // Custom tab component with close functionality built into the tab itself
    static class ClosableTabComponent extends JPanel {
        public ClosableTabComponent(final JTabbedPane tabbedPane, String title) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setOpaque(false);

            JLabel tabTitle = new JLabel(title);
            add(tabTitle);

            // Create close button
            JButton closeButton = new JButton("✖");
            closeButton.setBorder(BorderFactory.createEmptyBorder());
            closeButton.setFocusPainted(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setPreferredSize(new Dimension(17, 17));

            closeButton.addActionListener(e -> {
                int i = tabbedPane.indexOfTabComponent(ClosableTabComponent.this);
                if (i != -1) {
                    tabbedPane.remove(i);
                }
            });

            add(closeButton);
        }
    }
}
