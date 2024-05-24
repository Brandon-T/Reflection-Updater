package acid.other;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * Created by Kira on 2014-12-06.
 */
public class DeprecatedFinder {
    public static final int WILDCARD = -3;
    public static final int OPTIONAL = -4;
    public static final int CONSTANT = -5;
    public static final int VARIABLE = -6;
    public static final int NUMERIC = -7;
    public static final int COMPARISON = -8;
    public static final int COMPARISON2 = -9;
    public static final int ARITHMETIC = -10;
    public static final int MULTIPLY = -11;
    public static final int INVOCATION = -12;
    private InsnList instructions = null;
    private MethodNode m;

    public DeprecatedFinder(MethodNode method) {
        this.m = method;
        this.instructions = method.instructions;
    }

    public int findPrev(int start, int code, boolean skip_goto) {
        for (int i = start; i >= 0; --i) {
            if (instructions.get(i).getOpcode() == code) {
                return i;
            }

            if (isSpecialType(code, instructions.get(i).getOpcode())) {
                return i;
            }

            if (!skip_goto && instructions.get(i).getOpcode() == Opcodes.GOTO) {
                i = instructions.indexOf(((JumpInsnNode)instructions.get(i)).label);
                --i;
            }
        }
        return -1;
    }

    public int findNext(int start, int code) {
        return findNext(start, code, true);
    }

    public int findNext(int start, int code, boolean skip_goto) {
        for (int i = start; i < instructions.size(); ++i) {
            if (instructions.get(i).getOpcode() == code) {
                return i;
            }

            if (isSpecialType(code, instructions.get(i).getOpcode())) {
                return i;
            }

            if (!skip_goto && instructions.get(i).getOpcode() == Opcodes.GOTO) {
                i = instructions.indexOf(((JumpInsnNode)instructions.get(i)).label);
            }
        }
        return -1;
    }

    public int findPattern(int pattern[], int start, boolean skip_goto) {
        List<AbstractInsnNode> nodes = findPatternInstructions(pattern, start, skip_goto);
        if (nodes != null) {
            return instructions.indexOf(nodes.get(0));
        }
        return -1;
    }

    public List<AbstractInsnNode> findPatternInstructions(int pattern[], int start, boolean skip_goto) {
        int stackSize = instructions.size();
        int patternSize = pattern.length;
        int maxSize = stackSize - patternSize;
        List<AbstractInsnNode> foundInstructions = new ArrayList<>();

        for (int result = start; result <= maxSize; ++result) {
            int j = result;
            boolean found = true;
            foundInstructions.clear();

            for (int i = 0; i < patternSize; ++i, ++j) {
                if (j >= stackSize) {
                    found = false;
                    break;
                }

                AbstractInsnNode instruction = instructions.get(j);

                // Handle optional instructions
                if (pattern[i] == OPTIONAL) {
                    if (matchInstruction(pattern[i + 1], instruction)) {
                        --j;
                        continue;
                    }

                    if (matchInstruction(pattern[i + 1], instructions.get(j + 1))) {
                        continue;
                    }
                }

                // Handle GOTO instructions
                if (skip_goto) {
                    // Do nothing?
                    /*if (instruction.getOpcode() == Opcodes.GOTO) {
                        JumpInsnNode jumpInsnNode = (JumpInsnNode) instruction;
                        LabelNode labelNode = jumpInsnNode.label;
                        int offset = instructions.indexOf(labelNode);

                        // Skip over the GOTO + Label
                        j = offset + 1;
                        instruction = instructions.get(j);
                    }*/
                } else {
                    if (instruction.getOpcode() == Opcodes.GOTO) {
                        JumpInsnNode jumpInsnNode = (JumpInsnNode) instruction;
                        LabelNode labelNode = jumpInsnNode.label;
                        int offset = instructions.indexOf(labelNode);

                        // Match the continued pattern after following the GOTO
                        if (matchPattern(foundInstructions, pattern, i, offset + 1)) {
                            found = true;
                            break;
                        }

                        // No pattern was found, so skip over the GOTO + Label and continue matching the rest of the pattern
                        /*j = offset + 1;
                        instruction = instructions.get(j);
                        if (!matchInstruction(pattern[i], instruction)) {
                            found = false;
                            break;
                        }

                        foundInstructions.add(instruction);*/

                        found = false;
                        break;
                    }
                }

                if (!matchInstruction(pattern[i], instruction)) {
                    found = false;
                    break;
                }

                foundInstructions.add(instruction);
            }

            if (found) {
                return foundInstructions;
            }
        }

        return null;
    }

    private boolean matchInstruction(int patternOpcode, AbstractInsnNode instruction) {
        if (patternOpcode == WILDCARD) {
            return true;
        }

        if (isSpecial(patternOpcode)) {
            return isSpecialType(patternOpcode, instruction.getOpcode());
        }

        return patternOpcode == instruction.getOpcode();
    }

    private boolean matchPattern(List<AbstractInsnNode> foundInstructions, int[] pattern, int patternIndex, int instructionIndex) {
        for (int i = patternIndex, j = instructionIndex; i < pattern.length; ++i, ++j) {
            if (j >= instructions.size()) {
                return false;
            }

            if (pattern[i] == OPTIONAL) {
                if (matchInstruction(pattern[i + 1], instructions.get(j))) {
                    --j;
                    continue;
                }

                if (matchInstruction(pattern[i + 1], instructions.get(j + 1))) {
                    continue;
                }
            }

            if (!matchInstruction(pattern[i], instructions.get(j))) {
                return false;
            }

            foundInstructions.add(instructions.get(j));
        }

        return true;
    }

    private boolean isSpecial(int opcode) {
        return  opcode <= WILDCARD && opcode >= INVOCATION;
    }

    private boolean isSpecialType(int specialCode, int opcode) {
        switch(specialCode) {
            case VARIABLE: return (opcode >= Opcodes.ILOAD && opcode <= Opcodes.DLOAD);
            case CONSTANT: return ((opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5) || opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH || opcode == Opcodes.LDC);
            case NUMERIC: return (isSpecialType(VARIABLE, opcode) || isSpecialType(CONSTANT, opcode));
            case COMPARISON: return (opcode >= Opcodes.IF_ICMPEQ && opcode <= Opcodes.IF_ACMPNE);
            case COMPARISON2: return (opcode >= Opcodes.IFEQ && opcode <= Opcodes.IFLE);
            case ARITHMETIC: return (opcode >= Opcodes.IADD && opcode <= Opcodes.DDIV);
            case MULTIPLY: return (opcode >= Opcodes.IMUL && opcode <= Opcodes.DMUL);
            case INVOCATION: return (opcode >= Opcodes.INVOKEVIRTUAL && opcode <= Opcodes.INVOKEDYNAMIC);

            case WILDCARD: return true;
            case OPTIONAL: return true;

            default: return false;
        }
    }
}
