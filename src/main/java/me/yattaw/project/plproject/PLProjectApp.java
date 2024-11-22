package me.yattaw.project.plproject;

import com.formdev.flatlaf.FlatDarkLaf;
import me.yattaw.project.plproject.data.ObfData;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class PLProjectApp {

    private JTabbedPane tabbedPane;
    private static final Random random = new Random();

    private final Map<String, ObfData> nodeObfDataMap = new HashMap<>();
    private final Map<FieldNode, ObfData> fieldNodeObfDataMap = new HashMap<>();


    public static void main(String[] args) {
        SwingUtilities.invokeLater(PLProjectApp::new);
    }

    public PLProjectApp() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        AtomicReference<String> jarPath = new AtomicReference<>();

        // Create the main frame
        JFrame frame = new JFrame("PLProject Obfuscator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(new GridLayout(1, 2));  // Split the layout into two equal panels

        // Left panel with buttons and class list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        // Buttons for uploading jar and showing classes
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton uploadJarButton = new JButton("Upload jar");
        buttonPanel.add(uploadJarButton, BorderLayout.CENTER);

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

        // Obfuscate button
        JButton obfuscateButton = new JButton("Obfuscate Selected JAR");
        obfuscateButton.setEnabled(false);  // Disable until a JAR is loaded
        obfuscateButton.addActionListener(e -> {
            obfuscateJar(jarPath.get());
        });

        // Add the tabbedPane and obfuscateButton to the right panel
        rightPanel.add(tabbedPane, BorderLayout.CENTER);
        rightPanel.add(obfuscateButton, BorderLayout.SOUTH);  // Button sticks to the bottom and spans the width

        // Add the left and right panels to the frame
        frame.add(leftPanel);
        frame.add(rightPanel);

        // ActionListener for Upload Button (to load jar and classes dynamically)
        uploadJarButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                jarPath.set(filePath);
                try {
                    loadClassesFromJar(root, classTree, filePath);
                    obfuscateButton.setEnabled(true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Obfuscation button functionality
        obfuscateButton.addActionListener(e -> {
            // Add obfuscation code here
        });

        // Listener for selecting a method/field in the tree
        classTree.addTreeSelectionListener(e -> {
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
        });

        // Show the frame
        frame.setVisible(true);
    }

    private void loadClassesFromJar(DefaultMutableTreeNode root, JTree tree, String jarPath) throws IOException {
        root.removeAllChildren();  // Clear previous data

        // Open the JAR file
        try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jarPath))) {
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    ClassNode classNode = new ClassNode();
                    ClassReader classReader = new ClassReader(jarStream);
                    classReader.accept(classNode, 0);

                    // Create a node for the class
                    DefaultMutableTreeNode classTreeNode = new DefaultMutableTreeNode(classNode.name.replace('/', '.'));
                    root.add(classTreeNode);

                    // Add methods
                    DefaultMutableTreeNode methodsNode = new DefaultMutableTreeNode("Methods");
                    classTreeNode.add(methodsNode);
                    for (MethodNode method : classNode.methods) {
                        methodsNode.add(new DefaultMutableTreeNode(method.name + method.desc));

                        // Initialize ObfData for each MethodNode and add it to the nodeObfDataMap
                        ObfData methodObfData = new ObfData();
                        nodeObfDataMap.put(method.name + method.desc, methodObfData);
                    }

                    // Add fields
                    DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode("Fields");
                    classTreeNode.add(fieldsNode);
                    for (FieldNode field : classNode.fields) {
                        fieldsNode.add(new DefaultMutableTreeNode(field.name + " " + field.desc));

                        // Initialize ObfData for each FieldNode and add it to the fieldNodeObfDataMap
                        ObfData fieldObfData = new ObfData();
                        fieldNodeObfDataMap.put(field, fieldObfData);
                    }
                }
            }
        }

        // Update the JTree UI
        ((DefaultTreeModel) tree.getModel()).reload();
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
        ObfData obfData = getObfDataForTab(tabIdentifier);

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
        String bytecodeInstructions = getBytecodeInstructions(tabIdentifier);
        bytecodeTextArea.setText(bytecodeInstructions);
    }

    private String getBytecodeInstructions(String tabIdentifier) {
        // Retrieve MethodNode or FieldNode associated with the tabIdentifier
        for (Map.Entry<String, ObfData> entry : nodeObfDataMap.entrySet()) {
            if (tabIdentifier.contains(entry.getKey())) {
//                return getMethodBytecode(method);
            }
        }
        for (Map.Entry<FieldNode, ObfData> entry : fieldNodeObfDataMap.entrySet()) {
            FieldNode field = entry.getKey();
            if (tabIdentifier.contains(field.name)) {
                return "Fields do not contain executable bytecode.";
            }
        }
        return "Bytecode instructions not found for: " + tabIdentifier;
    }


    private String getMethodBytecode(MethodNode method) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(new Textifier());
        Set<String> uniqueInstructions = new LinkedHashSet<>();

        for (AbstractInsnNode insn : method.instructions) {
            // Trace and store unique instruction output
            insn.accept(traceMethodVisitor);

            // Clean up the traced output to prevent extra characters
            String instruction = traceMethodVisitor.p.getText().toString().replaceAll("[\\[\\]]", "").trim();

            if (uniqueInstructions.add(instruction)) { // add only if unique
                // Print each unique instruction with proper formatting
                printWriter.println(instruction);
            }

            // Clear buffer for next instruction
            traceMethodVisitor.p.getText().clear();
        }

        printWriter.flush();
        return stringWriter.toString();
    }

    // Helper method to find ObfData for the selected method/field
    private ObfData getObfDataForTab(String tabIdentifier) {
        // Assuming the tabIdentifier uniquely represents either a MethodNode or FieldNode
        for (Map.Entry<String, ObfData> entry : nodeObfDataMap.entrySet()) {
            if (tabIdentifier.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        for (Map.Entry<FieldNode, ObfData> entry : fieldNodeObfDataMap.entrySet()) {
            if (tabIdentifier.contains(entry.getKey().name)) {
                return entry.getValue();
            }
        }
        return null; // No matching ObfData found
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

    // Mockup of the obfuscateJar method
    public void obfuscateJar(String jarPath) {
        String outputPath = jarPath.replace(".jar", "_obfuscated.jar");

        try (JarFile jarFile = new JarFile(jarPath);
             FileOutputStream fos = new FileOutputStream(outputPath);
             JarOutputStream jos = new JarOutputStream(fos)) {

            // Iterate through each entry in the JAR
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                // Copy non-class files (e.g., resources) as-is
                if (!entry.getName().endsWith(".class")) {
                    jos.putNextEntry(new JarEntry(entry.getName()));
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        is.transferTo(jos);
                    }
                    continue;
                }

                // Process class files
                try (InputStream is = jarFile.getInputStream(entry)) {
                    byte[] modifiedClass = getBytes(is);
                    jos.putNextEntry(new JarEntry(entry.getName()));
                    jos.write(modifiedClass);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getBytes(InputStream is) throws IOException {
        ClassReader classReader = new ClassReader(is);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        // Create a remapper that generates new names for each method
        Remapper remapper = new Remapper() {
            @Override
            public String mapMethodName(String owner, String name, String descriptor) {
                // Extract the ObfData object for this method
                ObfData obfData = nodeObfDataMap.get(name + descriptor);

                if (obfData != null && obfData.isNameObfuscation()) {
                    // Check if the method is already obfuscated
                    if (obfData.getObfuscatedName() != null) {
                        return obfData.getObfuscatedName(); // Return the already-obfuscated name
                    }

                    // Generate a new obfuscated name and store it
                    String obfuscatedName = generateRandomString(12);
                    obfData.setObfuscatedName(obfuscatedName);
                    System.out.println("Obfuscating " + name + " -> " + obfuscatedName);
                    return obfuscatedName;
                }

                return name; // No obfuscation needed
            }
        };

        // Apply the remapper using ClassRemapper
        ClassRemapper classRemapper = new ClassRemapper(classWriter, remapper);
        classReader.accept(classRemapper, ClassReader.EXPAND_FRAMES);

        // Write the modified class back to the JAR
        byte[] modifiedClass = classWriter.toByteArray();
        return modifiedClass;
    }

    // Helper method to generate a random string for the method name
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}