package me.yattaw.project.plproject.obf;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.FieldNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Obfuscator {

    private final Map<String, ObfData> nodeObfDataMap = new HashMap<>();
    private final Map<String, ObfData> fieldNodeObfDataMap = new HashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final List<String> JAVA_KEYWORDS = Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native", "new", "null", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while"
    );

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

        // Create a remapper that generates new names for methods and fields, and handles XOR obfuscation
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
                    System.out.println("Obfuscating method " + name + " -> " + obfuscatedName);
                    return obfuscatedName;
                }

                return name; // No obfuscation needed
            }

            @Override
            public String mapFieldName(String owner, String name, String descriptor) {
                // Extract the ObfData object for this field
                ObfData obfData = fieldNodeObfDataMap.get(name + " " + descriptor);

                if (obfData != null && obfData.isXorObfuscation()) {
                    // Check if the field is already obfuscated
                    if (obfData.getObfuscatedName() != null) {
                        return obfData.getObfuscatedName(); // Return the already-obfuscated name
                    }

                    // Generate a new obfuscated name and store it
                    String obfuscatedName = generateRandomString(12);
                    obfData.setObfuscatedName(obfuscatedName);
                    System.out.println("Obfuscating field " + name + " -> " + obfuscatedName);
                    return obfuscatedName;
                }

                return name; // No obfuscation needed
            }
        };

        // Apply the remapper using a ClassRemapper
        ClassVisitor classRemapper = new ClassRemapper(classWriter, remapper) {
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                FieldVisitor fieldVisitor = super.visitField(access, name, descriptor, signature, null);

                ObfData obfData = fieldNodeObfDataMap.get(name + " " + descriptor);
                if (obfData != null && obfData.isXorObfuscation()) {
                    return new FieldVisitor(Opcodes.ASM9, fieldVisitor) {
                        @Override
                        public void visitEnd() {
                            super.visitEnd();
                            System.out.println("XOR obfuscation applied to field: " + name);
                        }
                    };
                }

                return fieldVisitor; // No XOR obfuscation
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

                // Check for static initializer (<clinit>) to apply XOR obfuscation logic
                if ("<clinit>".equals(name)) {
                    return new MethodVisitor(Opcodes.ASM9, methodVisitor) {
                        @Override
                        public void visitCode() {
                            super.visitCode();

                            // Apply XOR obfuscation for all applicable fields
                            for (Map.Entry<String, ObfData> entry : fieldNodeObfDataMap.entrySet()) {
                                ObfData obfData = entry.getValue();
                                if (obfData.isXorObfuscation()) {
                                    String obfuscatedName = obfData.getObfuscatedName();
                                    if (obfuscatedName != null) {
                                        // Load XOR obfuscated value (e.g., 8944320 XOR 8943912)
                                        super.visitLdcInsn(8944320); // Load constant 1
                                        super.visitLdcInsn(8943912); // Load constant 2
                                        super.visitInsn(Opcodes.IXOR); // Perform XOR
                                        super.visitFieldInsn(Opcodes.PUTSTATIC, classReader.getClassName(), obfuscatedName, "I");
                                    }
                                }
                            }
                        }

                        @Override
                        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                            // Skip redundant field initialization in the static initializer
                            ObfData obfData = fieldNodeObfDataMap.get(name);
                            if (opcode == Opcodes.PUTSTATIC && obfData != null && obfData.isXorObfuscation()) {
                                return; // Skip the original static initialization
                            }
                            super.visitFieldInsn(opcode, owner, name, descriptor);
                        }
                    };
                }

                return methodVisitor; // No static initializer to modify
            }
        };

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