package acid.other;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by Kira on 2014-12-06.
 */
public class Finder {
    public static final int WILDCARD = -3;
    public static final int OPTIONAL = -4;
    public static final int CONSTANT = -5;
    public static final int VARIABLE = -6;
    public static final int NUMERIC = -7;
    public static final int COMPARISON = -8;
    public static final int COMPARISON2 = -9;
    public static final int ARITHMETIC = -10;
    public static final int MULTIPLY = -11;
    private InsnList instructions = null;

    public Finder(MethodNode method) {
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
        ArrayList<AbstractInsnNode> arr = new ArrayList<>(
                Arrays.asList(Arrays.copyOfRange(instructions.toArray(), start, instructions.size()))
        );

        int result = search(arr, sub, 0, skip_goto, (a, b) -> {
            if (isSpecial(b)) {
                return isSpecialType(b, a.getOpcode());
            }
            return a.getOpcode() == b;
        });
        return result != -1 ? result + start : -1;
    }

    public int matchPatternOld(int sub[], int start, boolean skip_goto) {
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

                if (sub[l] == WILDCARD && (k + 1 >= arr.length)) {
                    break;
                }

                if (sub[l] == OPTIONAL) {
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
        return  opcode <= WILDCARD && opcode >= MULTIPLY;
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

            case WILDCARD: return true;
            case OPTIONAL: return true;

            default:
                return false;
        }
    }

    private static int search(int[] haystack, int[] needle, BiFunction<Integer, Integer, Boolean> predicate) {
        int first = 0;
        while(true) {
            int it = first;
            for (int s_it = 0; ; ++it, ++s_it) {
                //Found
                if (s_it == needle.length) {
                    return first;
                }

                //Not Found
                if (it == haystack.length) {
                    return -1;
                }

                //DIRECT MATCH
                if (predicate != null) {
                    if (!predicate.apply(haystack[it], needle[s_it])) {
                        break;
                    }
                }
                else if (haystack[it] != needle[s_it]) {
                    break;
                }
            }

            ++first;
        }
    }

    private int search(ArrayList<AbstractInsnNode> haystack, int[] needle, int start, boolean skip_goto, BiFunction<AbstractInsnNode, Integer, Boolean> predicate) {
        BiFunction<Integer, Integer, Integer> find = (Integer first, Integer opcode) -> {
            for (int i = first; i < haystack.size(); ++i) {
                if (predicate.apply(haystack.get(i), opcode)) {
                    return i;
                }
            }
            return -1;
        };

        for (int i = find.apply(start, needle[0]); i >= 0 && i < haystack.size();) {
            int it = i;
            int j = 0;
            for (; j < needle.length; ++j, ++it) {
                if (it == -1 || it >= haystack.size()) {
                    break;
                }

                //GOTO
                if (haystack.get(it).getOpcode() == Opcodes.GOTO) {
                    if (skip_goto) {
                        //Skip the GOTO + Label
                        ++it;
                    } else {
                        //Follow the GOTO pointed by its Label
                        it = haystack.indexOf(((JumpInsnNode)haystack.get(it)).label.getNext());
                    }

                    if (it == -1 || it >= haystack.size()) {
                        break;
                    }
                }

                //LABEL
                while (haystack.get(it).getType() == AbstractInsnNode.LABEL) {
                    if (++it >= haystack.size()) {
                        break;
                    }
                }

                //WILDCARD
                if (needle[j] == WILDCARD) {
                    continue;
                }

                //OPTIONAL
                if (needle[j] == OPTIONAL) {
                    if (j == needle.length - 1) {
                        break;
                    }

                    if (haystack.get(it).getOpcode() == needle[j + 1]) {
                        ++j;
                        continue;
                    }

                    if (haystack.get(it + 1).getOpcode() == needle[j + 1]) {
                        ++j;
                        ++it;
                        continue;
                    }

                    break;
                }

                if (j >= needle.length || it >= haystack.size() || !predicate.apply(haystack.get(it), needle[j])) {
                    break;
                }
            }

            if (j == needle.length) {
                return i;
            }

            i = find.apply(i + 1, needle[0]);
        }
        return -1;
    }
}
