# PLProject Obfuscator

## Overview

The PLProject Obfuscator is a Java-based tool designed for educational analysis of Java bytecode obfuscation. It loads compiled Java classes from a JAR file, provides a Java swing interface for viewing class structures and methods, and allows users to apply various obfuscation techniques to understand their effects on bytecode.

## Features

- **JAR File Loading:** Users can upload JAR files, and the tool dynamically loads and displays the classes, methods, and fields within.
- **Bytecode Viewer:** Select methods or fields to view their bytecode instructions.
- **Obfuscation Options:** Supports simple obfuscation techniques with user-selectable options.
  - XOR Field Obfuscation
  - Method Name Obfuscation
- **UI:** Interactive Swing interface using `FlatDarkLaf` for a modern look and feel.
- **Closable Tabs:** Bytecode instructions are displayed in tabs that can be closed individually.

## Code Structure

### Packages and Libraries

- **`org.objectweb.asm`**: ASM library for Java bytecode manipulation, enabling reading, modifying, and displaying bytecode instructions.
- **`com.formdev.flatlaf`**: Used for UI styling with a dark theme.

### Obfuscation Techniques

Each method and field node has an associated `ObfData` object, which stores the selected obfuscation options:
- **XOR Field Obfuscation**
- **Method Name Obfuscation**

These options are updated dynamically based on user selections, allowing real-time obfuscation configuration for each method or field.

## Usage Instructions

1. **Load JAR File**:
   - Click the "Upload jar" button to select and load a JAR file.
   - The left panel displays a tree structure with classes, methods, and fields from the loaded JAR.

2. **View Bytecode**:
   - Select any method or field from the tree. A new tab will open displaying bytecode instructions if available.

3. **Configure Obfuscation**:
   - In each method/field tab, select the obfuscation options you want to apply (e.g., XOR Field Obfuscation, Method Name Obfuscation).

4. **Execute Obfuscation**:
   - Click the "Obfuscate Selected JAR" button. A new jar will be compiled with the bytecode modifications.

## Dependencies

- Java 8 or higher
- [ASM Library](https://asm.ow2.io/)
- [FlatLaf](https://www.formdev.com/flatlaf/)
