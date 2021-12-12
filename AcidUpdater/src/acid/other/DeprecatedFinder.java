package acid.other;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.function.Function;

/**
 * Created by Kira on 2014-12-06.
 */
public class DeprecatedFinder {
    private InsnList instructions = null;

    public DeprecatedFinder(MethodNode method) {
        this.instructions = method.instructions;
    }

    public int findPrev(int start, int code) {
        AbstractInsnNode[] nodes = instructions.toArray();
        for (int i = start; i >= 0; --i) {
            if (nodes[i].getOpcode() == code) {
                return i;
            }

            if (isSpecialType(code, nodes[i].getOpcode())) {
                return i;
            }
        }
        return -1;
    }

    public int findNext(int start, int code) {
        return findNext(start, code, true);
    }

    public int findNext(int start, int code, boolean skip_goto) {
        AbstractInsnNode[] nodes = instructions.toArray();
        for (int i = start; i < nodes.length; ++i) {
            if (nodes[i].getOpcode() == code) {
                return i;
            }

            if (isSpecialType(code, nodes[i].getOpcode())) {
                return i;
            }

            if (!skip_goto && nodes[i].getOpcode() == Opcodes.GOTO) {
                i = instructions.indexOf(((JumpInsnNode)nodes[i]).label.getNext());
                --i;
            }
        }
        return -1;
    }

    public int findNextInstruction(int start, int code, int afterAmount) {
        int count = 0;

        while (true) {
            start = findNext(start, code, false);

            if (start != -1) {
                if (count == afterAmount) {
                    return start;
                }
                ++count;
                ++start;
            }
            else {
                break;
            }
        }
        return -1;
    }

    public int findPattern(int sub[]) {
        return findPattern(sub, 0);
    }

    public int findPattern(int sub[], int start) {
        return findPattern(sub, start, true);
    }

    public int findPattern(int sub[], int start, boolean skip_goto) {
        AbstractInsnNode arr[] = instructions.toArray();

        for (int i = start; i < arr.length; ++i) {
            int k = i, l = 0;

            while(k < arr.length && ((arr[k].getOpcode() == Opcodes.GOTO) || arr[k].getOpcode() == sub[l] || isSpecial(sub[l]))) {
                if (arr[k].getOpcode() == Opcodes.GOTO) {
                    if (skip_goto) {
                        k = instructions.indexOf(((JumpInsnNode) arr[k]).label);
                        continue;
                    }
                    else {
                        k = instructions.indexOf(((JumpInsnNode) arr[k]).label.getNext());
                    }
                }

                if (isSpecial(sub[l])) {
                    if (!isSpecialType(sub[l], arr[k].getOpcode())) {
                        break;
                    }
                }

                if (sub[l] == Finder.WILDCARD && (k + 1 >= arr.length)) {
                    break;
                }

                if (sub[l] == Finder.OPTIONAL) {
                    if (arr[k].getOpcode() == sub[l + 1]) {
                        ++l;
                        continue;
                    } else if ((k + 1 < arr.length) && arr[k + 1].getOpcode() == sub[l + 1]) {
                        ++k; ++l;
                        continue;
                    }
                    break;
                }

                if (++l == sub.length) {
                    if (arr[i].getOpcode() == Opcodes.GOTO && arr[i].getOpcode() != sub[l - 1] && !skip_goto) {
                        i = instructions.indexOf(((JumpInsnNode) arr[i]).label.getNext());
                    }
                    return i;
                }

                if (++k == arr.length) {
                    return -1; //return 0; Changed on January 21, 2020 to return -1.
                }

                if (!skip_goto && arr[k].getOpcode() == -1) {
                    ++k;
                }
            }
        }
        return -1;
    }

    public long findMultiplier(String owner, String field) {
        int patterns[][] = new int[][] {
                {Opcodes.LDC, Opcodes.ALOAD, Opcodes.GETFIELD},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC},
                {Opcodes.LDC, Opcodes.GETSTATIC},
                {Opcodes.GETSTATIC, Opcodes.LDC},
                {Opcodes.LDC, Opcodes.GETFIELD},
                {Opcodes.GETFIELD, Opcodes.LDC}
        };

        Function<int[], Object> internal_find = (pattern) -> {
            int i = findPattern(pattern);
            while (i != -1) {
                int op_code = instructions.get(i + pattern.length).getOpcode();
                if (op_code == Opcodes.IMUL || op_code == Opcodes.LMUL) {
                    FieldInsnNode f = null;
                    if (instructions.get(i) instanceof FieldInsnNode) {
                        f = (FieldInsnNode) instructions.get(i);
                    } else if (instructions.get(i + 1) instanceof FieldInsnNode) {
                        f = (FieldInsnNode) instructions.get(i + 1);
                    } else {
                        f = (FieldInsnNode) instructions.get(i + 2);
                    }

                    if ((f.desc.equals("I") || f.desc.equals("J")) && f.owner.equals(owner) && f.name.equals(field)) {
                        if (instructions.get(i) instanceof LdcInsnNode) {
                            return ((LdcInsnNode) instructions.get(i)).cst;
                        } else if (instructions.get(i + 1) instanceof LdcInsnNode) {
                            return ((LdcInsnNode) instructions.get(i + 1)).cst;
                        }
                        return ((LdcInsnNode) instructions.get(i + 2)).cst;
                    }
                }
                i = findPattern(pattern, i + 1);
            }
            return 0;
        };

        for (int[] pattern : patterns) {
            Object multi = internal_find.apply(pattern);
            if (multi instanceof Long && ((long)multi != 0)) {
                return (long)multi;
            } else if ((int)multi != 0) {
                return (int)multi;
            }
        }

        return 0;
    }

    private boolean isSpecial(int opcode) {
        return  opcode <= Finder.WILDCARD && opcode >= Finder.MULTIPLY;
    }

    private boolean isSpecialType(int specialCode, int opcode) {
        switch(specialCode) {
            case Finder.VARIABLE: return (opcode >= Opcodes.ILOAD && opcode <= Opcodes.DLOAD);
            case Finder.CONSTANT: return ((opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5) || opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH || opcode == Opcodes.LDC);
            case Finder.NUMERIC: return (isSpecialType(Finder.VARIABLE, opcode) || isSpecialType(Finder.CONSTANT, opcode));
            case Finder.COMPARISON: return (opcode >= Opcodes.IF_ICMPEQ && opcode <= Opcodes.IF_ACMPNE);
            case Finder.COMPARISON2: return (opcode >= Opcodes.IFEQ && opcode <= Opcodes.IFLE);
            case Finder.ARITHMETIC: return (opcode >= Opcodes.IADD && opcode <= Opcodes.DDIV);
            case Finder.MULTIPLY: return (opcode >= Opcodes.IMUL && opcode <= Opcodes.DMUL);

            case Finder.WILDCARD: return true;
            case Finder.OPTIONAL: return true;

            default:
                return false;
        }
    }
}
