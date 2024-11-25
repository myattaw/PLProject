package me.yattaw.project.plproject.obf;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.FieldNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Obfuscator {


    private final Map<String, ObfData> nodeObfDataMap = new HashMap<>();
    private final Map<String, ObfData> fieldNodeObfDataMap = new HashMap<>();
    private static final Random RANDOM = new Random();

    /**
     * Obfuscates the provided JAR file.
     *
     * @param jarPath Path to the input JAR file.
     */
    public boolean obfuscateJar(String jarPath) {
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
                    byte[] modifiedClass = applyObfuscation(is);
                    jos.putNextEntry(new JarEntry(entry.getName()));
                    jos.write(modifiedClass);
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error obfuscating JAR file", e);
        }
        return false;
    }

    /**
     * Applies obfuscation transformations to a class file.
     *
     * @param inputStream InputStream of the class file.
     * @return Transformed byte array of the class file.
     * @throws IOException If an error occurs during reading.
     */
    private byte[] applyObfuscation(InputStream inputStream) throws IOException {
        ClassReader classReader = new ClassReader(inputStream);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        // Create a remapper that generates new names for methods and handles transformations
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

        // Apply the remapper using a ClassRemapper
        ClassRemapper classRemapper = new ClassRemapper(classWriter, remapper);
        classReader.accept(classRemapper, ClassReader.EXPAND_FRAMES);

        return classWriter.toByteArray();
    }

    /**
     * Generates a random string for obfuscated names.
     *
     * @param length Length of the random string.
     * @return Randomly generated string.
     */
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public Map<String, ObfData> getFieldNodeObfDataMap() {
        return fieldNodeObfDataMap;
    }

    public Map<String, ObfData> getNodeObfDataMap() {
        return nodeObfDataMap;
    }

}