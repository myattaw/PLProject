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

## Before Obfuscation

```java
// Decompiled with: FernFlower
// Class Version: 17
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static int ONE_THOUSAND = 1000;

    public static void main(String[] args) {
        int limit = 10000;
        List<Integer> perfectNumbers = (List)IntStream.range(1, limit + 1).filter(Main::isPerfect).boxed().collect(Collectors.toList());
        Main main = new Main();
        main.loop();
        System.out.println("One Thousand: " + ONE_THOUSAND);
        System.out.println("Perfect numbers up to " + limit + ": " + perfectNumbers);
    }

    private static boolean isPerfect(int number) {
        int sumOfDivisors = IntStream.range(1, number).filter((i) -> number % i == 0).sum();
        return sumOfDivisors == number;
    }

    public void loop() {
        System.out.println("1,000 - 5,000:");

        for(int i = 1; i <= 5; ++i) {
            System.out.println("num: " + i * ONE_THOUSAND);
        }

    }
}
```

## After Obfuscation
```java
// Decompiled with: FernFlower
// Class Version: 17
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static int crOvYeElYpSD = 8944320 ^ 8943912;

    public static void main(String[] args) {
        int limit = 10000;
        List<Integer> perfectNumbers = (List)IntStream.range(1, limit + 1).filter(Main::gBmADgDiIsIu).boxed().collect(Collectors.toList());
        Main main = new Main();
        main.SFXQtLrKcyKc();
        System.out.println("One Thousand: " + crOvYeElYpSD);
        System.out.println("Perfect numbers up to " + limit + ": " + perfectNumbers);
    }

    private static boolean gBmADgDiIsIu(int number) {
        int sumOfDivisors = IntStream.range(1, number).filter((i) -> number % i == 0).sum();
        return sumOfDivisors == number;
    }

    public void SFXQtLrKcyKc() {
        System.out.println("1,000 - 5,000:");

        for(int i = 1; i <= 5; ++i) {
            System.out.println("num: " + i * crOvYeElYpSD);
        }

    }

    static {
        boolean var10000 = true;
    }
}
```

## Issues
- Currently, the obfuscated JAR retains traces of the field's original value in the bytecode, which should not be there. 
When decompiling the JAR, a random SIPUSH instruction appears, causing the decompiler to mistakenly add an unrelated variable.
- There is no way to effectively obfuscate constants because the compiler optimizes these constants by merging them into parameters. 
As a result, if I obfuscate a constant, it is likely that there will be no reference to that constant in the bytecode.

## Bonus:
The code below can still be executed even though it contains syntax errors. This is because Java method names can be 
remapped to use Java keywords without breaking the program. While Java keywords are composed of a sequence of Unicode characters,
my remapper uses ASCII characters, which confuses decompilers into interpreting keywords as method names as shown below.

```java
// Decompiled with: FernFlower
// Class Version: 17
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static int ONE_THOUSAND = 1000;

    public static void main(String[] args) {
        int limit = 10000;
        List<Integer> perfectNumbers = (List)IntStream.range(1, limit + 1).filter(Main::final).boxed().collect(Collectors.toList());
        Main main = new Main();
        main.byte();
        System.out.println("One Thousand: " + ONE_THOUSAND);
        System.out.println("Perfect numbers up to " + limit + ": " + perfectNumbers);
    }

    private static boolean final(int number) {
        int sumOfDivisors = IntStream.range(1, number).filter((i) -> number % i == 0).sum();
        return sumOfDivisors == number;
    }

    public void byte() {
        System.out.println("1,000 - 5,000:");

        for(int i = 1; i <= 5; ++i) {
            System.out.println("num: " + i * ONE_THOUSAND);
        }

    }
}
```

## Dependencies

- Java 8 or higher
- [ASM Library](https://asm.ow2.io/)
- [FlatLaf](https://www.formdev.com/flatlaf/)
