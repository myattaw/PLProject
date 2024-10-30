package me.yattaw.project.plproject.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class InstructionHelper {

    // Cache for opcode names
    private static final Map<Integer, String> opcodeToName = new HashMap<>();
    private static final Map<Class<?>, Function<AbstractInsnNode, String>> formatByType = new HashMap<>();

    static {
        // Populate the opcode-to-name map at class loading time
        for (Field field : Opcodes.class.getDeclaredFields()) {
            if (field.getType().equals(int.class)) {
                try {
                    int opcode = field.getInt(null);
                    opcodeToName.put(opcode, field.getName());
                } catch (IllegalAccessException e) {
                    System.err.println("Failed to access opcode field: " + field.getName());
                }
            }
        }

        // Initialize formatByType mappings
        formatByType.put(MethodInsnNode.class, InstructionHelper::formatMethodInsn);
        formatByType.put(VarInsnNode.class, InstructionHelper::formatVarInsn);
        formatByType.put(InsnNode.class, InstructionHelper::formatInsn);
        formatByType.put(JumpInsnNode.class, InstructionHelper::formatJumpInsn);
        formatByType.put(LabelNode.class, insn -> "LABEL L"); // LabelNode handling improved
        formatByType.put(LineNumberNode.class, InstructionHelper::formatLineNumberNode);
        formatByType.put(FrameNode.class, insn -> "FRAME");
        formatByType.put(LdcInsnNode.class, InstructionHelper::formatLdcInsn);
        formatByType.put(IntInsnNode.class, InstructionHelper::formatIntInsn);
        formatByType.put(TypeInsnNode.class, InstructionHelper::formatTypeInsn);
        formatByType.put(FieldInsnNode.class, InstructionHelper::formatFieldInsn);
    }

    /**
     * Retrieves the name of an opcode.
     *
     * @param opcode The opcode integer value to look up.
     * @return The string name of the opcode or "UNKNOWN_OPCODE_" + opcode if not found.
     */
    public static String getOpcodeName(int opcode) {
        return opcodeToName.getOrDefault(opcode, "UNKNOWN_OPCODE_" + opcode);
    }

    // Individual formatting methods for each bytecode type

    // Method call instructions
    private static String formatMethodInsn(AbstractInsnNode insn) {
        MethodInsnNode methodInsn = (MethodInsnNode) insn;
        return String.format("INVOKESTATIC %s.%s%s", methodInsn.owner.replace('/', '.'), methodInsn.name, methodInsn.desc);
    }

    // Variable load/store instructions (e.g., ILOAD, ISTORE)
    private static String formatVarInsn(AbstractInsnNode insn) {
        VarInsnNode varInsn = (VarInsnNode) insn;
        return String.format("%s %d", getOpcodeName(varInsn.getOpcode()), varInsn.var);
    }

    // Simple instructions (e.g., ICONST, POP)
    private static String formatInsn(AbstractInsnNode insn) {
        InsnNode insnNode = (InsnNode) insn;
        return getOpcodeName(insnNode.getOpcode());
    }

    // Jump instructions (e.g., IF_ICMPNE, GOTO)
    private static String formatJumpInsn(AbstractInsnNode insn) {
        JumpInsnNode jumpInsn = (JumpInsnNode) insn;

        // Convert label to a string format. Assuming label ID counter is managed separately.
        String labelName = jumpInsn.label.toString(); // Using label's `toString()` method here

        return String.format("%s %s", getOpcodeName(jumpInsn.getOpcode()), labelName);
    }


    // Line number annotations
    private static String formatLineNumberNode(AbstractInsnNode insn) {
        LineNumberNode lineNumberNode = (LineNumberNode) insn;
        return "LINE " + lineNumberNode.line;
    }

    // Load constant (LDC) instructions
    private static String formatLdcInsn(AbstractInsnNode insn) {
        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
        return String.format("LDC %s", ldcInsn.cst);
    }

    // Integer instructions (e.g., BIPUSH, SIPUSH)
    private static String formatIntInsn(AbstractInsnNode insn) {
        IntInsnNode intInsn = (IntInsnNode) insn;
        return String.format("%s %d", getOpcodeName(intInsn.getOpcode()), intInsn.operand);
    }

    // Type instructions (e.g., NEW, CHECKCAST)
    private static String formatTypeInsn(AbstractInsnNode insn) {
        TypeInsnNode typeInsn = (TypeInsnNode) insn;
        return String.format("%s %s", getOpcodeName(typeInsn.getOpcode()), typeInsn.desc.replace('/', '.'));
    }

    // Field instructions (e.g., GETFIELD, PUTFIELD)
    private static String formatFieldInsn(AbstractInsnNode insn) {
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        return String.format("%s %s.%s %s", getOpcodeName(fieldInsn.getOpcode()), fieldInsn.owner.replace('/', '.'), fieldInsn.name, fieldInsn.desc);
    }

    /**
     * Format an instruction node based on its type using formatByType.
     *
     * @param insn The instruction node to format.
     * @param labelCounter The label counter for label nodes.
     * @return The formatted instruction as a string.
     */
    public static String formatInstruction(AbstractInsnNode insn, int labelCounter, Map<Integer, String> lineNumbers) {
        Function<AbstractInsnNode, String> formatter = formatByType.get(insn.getClass());

        if (formatter != null) {
            return formatter.apply(insn);
        }

        return "UNKNOWN_INSTRUCTION"; // Default case for unrecognized instructions
    }

}
