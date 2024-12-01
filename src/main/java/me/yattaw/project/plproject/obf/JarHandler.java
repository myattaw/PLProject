package me.yattaw.project.plproject.obf;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class JarHandler {

    /**
     * Loads classes and other entries from a JAR file into a tree structure.
     *
     * @param root    Root node of the JTree where JAR entries will be added.
     * @param tree    JTree component to display the loaded structure.
     * @param jarPath Path to the JAR file.
     * @throws IOException If the JAR file cannot be read.
     */
    public void loadClassesFromJar(Obfuscator obfuscator, DefaultMutableTreeNode root, JTree tree, String jarPath) throws IOException {
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
                        obfuscator.getNodeObfDataMap().put(method.name + method.desc, methodObfData);
                        obfuscator.getMethodNodeMap().put(method.name + method.desc, method);
                    }

                    // Add fields
                    DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode("Fields");
                    classTreeNode.add(fieldsNode);
                    for (FieldNode field : classNode.fields) {
                        fieldsNode.add(new DefaultMutableTreeNode(field.name + " " + field.desc));

                        // Initialize ObfData for each FieldNode and add it to the fieldNodeObfDataMap
                        ObfData fieldObfData = new ObfData();
                        obfuscator.getFieldNodeObfDataMap().put(field.name + " " + field.desc, fieldObfData);
                        obfuscator.getFieldNodeMap().put(field.name + " " + field.desc, field);
                    }
                }
            }
        }

        // Update the JTree UI
        ((DefaultTreeModel) tree.getModel()).reload();
    }

    /**
     * Finds a child node with the specified name under the given parent node.
     *
     * @param parent Parent node to search within.
     * @param name   Name of the child node to find.
     * @return The child node if found, or {@code null} if not.
     */
    private DefaultMutableTreeNode findChildNode(DefaultMutableTreeNode parent, String name) {
        Enumeration<?> children = parent.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            if (child.getUserObject().equals(name)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Returns a unique tab identifier based on the provided tree node.
     *
     * @param node The tree node to generate the identifier for.
     * @return A unique tab identifier.
     */
    public String getTabIdentifier(DefaultMutableTreeNode node) {
        StringBuilder identifier = new StringBuilder();

        // Traverse up the tree to construct the full path for the node
        DefaultMutableTreeNode currentNode = node;
        while (currentNode != null) {
            identifier.insert(0, currentNode.getUserObject().toString());
            currentNode = (DefaultMutableTreeNode) currentNode.getParent();

            if (currentNode != null) {
                identifier.insert(0, "/");
            }
        }

        return identifier.toString();
    }

}