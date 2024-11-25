package me.yattaw.project.plproject.ui;

import me.yattaw.project.plproject.obf.JarHandler;
import me.yattaw.project.plproject.obf.ObfData;
import me.yattaw.project.plproject.obf.Obfuscator;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class PLProjectUIManager {

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final AtomicReference<String> jarPath = new AtomicReference<>();
    private final Obfuscator obfuscator = new Obfuscator();
    private final JarHandler jarHandler = new JarHandler();
    private final Map<String, Component> openTabs = new HashMap<>(); // Track open tabs

    public PLProjectUIManager() {
        JFrame frame = new JFrame("PLProject Obfuscator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(new GridLayout(1, 2));

        // Initialize UI Panels
        JPanel leftPanel = initializeLeftPanel();
        JPanel rightPanel = initializeRightPanel();

        frame.add(leftPanel);
        frame.add(rightPanel);
        frame.setVisible(true);
    }

    private JPanel initializeLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        // Button panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton uploadJarButton = new JButton("Upload JAR");
        buttonPanel.add(uploadJarButton, BorderLayout.CENTER);

        // Class Tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Classes");
        JTree classTree = new JTree(root);
        JScrollPane scrollPane = new JScrollPane(classTree);

        // Add event listeners
        uploadJarButton.addActionListener(e -> handleUploadButton(root, classTree));
        classTree.addTreeSelectionListener(e -> handleClassTreeSelection(classTree));

        leftPanel.add(buttonPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        return leftPanel;
    }

    private JPanel initializeRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        // Obfuscate Button
        JButton obfuscateButton = new JButton("Obfuscate Selected JAR");
        obfuscateButton.setEnabled(true); // Set enabled since obfuscation can always occur
        obfuscateButton.addActionListener(e -> handleObfuscateButton());

        rightPanel.add(tabbedPane, BorderLayout.CENTER);
        rightPanel.add(obfuscateButton, BorderLayout.SOUTH);
        return rightPanel;
    }

    private void handleUploadButton(DefaultMutableTreeNode root, JTree classTree) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            jarPath.set(fileChooser.getSelectedFile().getAbsolutePath());
            try {
                jarHandler.loadClassesFromJar(obfuscator, root, classTree, jarPath.get());
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to load JAR: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleClassTreeSelection(JTree classTree) {
        DefaultMutableTreeNode selectedNode =
                (DefaultMutableTreeNode) classTree.getLastSelectedPathComponent();
        if (selectedNode != null && selectedNode.isLeaf()) {
            String tabIdentifier = jarHandler.getTabIdentifier(selectedNode);
            if (!isTabOpen(tabIdentifier)) {
                createClosableTab(tabIdentifier);
            }
        }
    }

    private void handleObfuscateButton() {
        if (jarPath.get() != null) {
            if (obfuscator.obfuscateJar(jarPath.get())) {
                JOptionPane.showMessageDialog(null, "Obfuscation completed successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "No JAR file selected for obfuscation.",
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private boolean isTabOpen(String tabIdentifier) {
        return openTabs.containsKey(tabIdentifier);
    }

    private void createClosableTab(String tabIdentifier) {
        JPanel tabPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel(tabIdentifier);
        tabPanel.add(infoLabel, BorderLayout.NORTH);

        // Create obfuscation options
        JPanel obfuscationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JCheckBox xorObfuscation = new JCheckBox("XOR Field Obfuscation");
        JCheckBox methodRemapper = new JCheckBox("Method Name Obfuscation");
        JCheckBox stringEncryption = new JCheckBox("String Encryption");

        // Retrieve the associated ObfData object
        ObfData obfData = getObfDataForTab(obfuscator, tabIdentifier);

        // Initialize checkboxes to match current ObfData settings
        if (obfData != null) {
            xorObfuscation.setSelected(obfData.isXorObfuscation());
            methodRemapper.setSelected(obfData.isNameObfuscation());
            stringEncryption.setSelected(obfData.isStringObfuscation());
        }

        // Listener for xorObfuscation checkbox
        xorObfuscation.addActionListener(e -> {
            if (obfData != null) {
                obfData.setXorObfuscation(xorObfuscation.isSelected());
                System.out.println("XOR Obfuscation set to: " + xorObfuscation.isSelected());
            }
        });

        // Listener for methodRemapper checkbox
        methodRemapper.addActionListener(e -> {
            if (obfData != null) {
                obfData.setNameObfuscation(methodRemapper.isSelected());
                System.out.println("Method Name Obfuscation set to: " + methodRemapper.isSelected());
            }
        });

        // Listener for stringEncryption checkbox
        stringEncryption.addActionListener(e -> {
            if (obfData != null) {
                obfData.setStringObfuscation(stringEncryption.isSelected());
                System.out.println("String Encryption set to: " + stringEncryption.isSelected());
            }
        });

        obfuscationPanel.add(xorObfuscation);
        obfuscationPanel.add(methodRemapper);
        obfuscationPanel.add(stringEncryption);

        tabPanel.add(obfuscationPanel, BorderLayout.CENTER);

        // Text area to display bytecode instructions
        JTextArea bytecodeTextArea = new JTextArea(10, 30);
        bytecodeTextArea.setEditable(false);
        bytecodeTextArea.setLineWrap(true);
        bytecodeTextArea.setWrapStyleWord(true);

        JScrollPane bytecodeScrollPane = new JScrollPane(bytecodeTextArea);
        tabPanel.add(bytecodeScrollPane, BorderLayout.SOUTH);

        // Add the tab with closable functionality
        tabbedPane.addTab(tabIdentifier, tabPanel);
        int index = tabbedPane.indexOfComponent(tabPanel);
        tabbedPane.setTabComponentAt(index, new ClosableTabComponent(tabbedPane, tabIdentifier));

        // Populate bytecodeTextArea with bytecode for the method or field
//        String bytecodeInstructions = getBytecodeInstructions(tabIdentifier);
//        bytecodeTextArea.setText(bytecodeInstructions);
    }


    // Helper method to find ObfData for the selected method/field
    private ObfData getObfDataForTab(Obfuscator obfuscator, String tabIdentifier) {
        // Assuming the tabIdentifier uniquely represents either a MethodNode or FieldNode
        for (Map.Entry<String, ObfData> entry : obfuscator.getNodeObfDataMap().entrySet()) {
            if (tabIdentifier.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        for (Map.Entry<String, ObfData> entry : obfuscator.getFieldNodeObfDataMap().entrySet()) {
            if (tabIdentifier.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        System.out.println("No matching ObfData found for tab: " + tabIdentifier);

        return null; // No matching ObfData found
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
