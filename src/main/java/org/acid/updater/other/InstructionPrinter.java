package org.acid.updater.other;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Kira on 2014-12-16.
 */
public class InstructionPrinter {
    private static final HashMap<Integer, String> code_map = new HashMap<>();

    public InstructionPrinter(MethodNode method) {
        printInstructions(method, 0, method.instructions.size());
    }

    public static void printInstructions(MethodNode method, int start, int end) {
        System.out.println(String.format("METHOD: %s %s%s", getAccess(method.access), method.name, method.desc));
        System.out.println("--------------------------------------\n");
        printInstructions(method.instructions.toArray(), start, end);
        System.out.println("}");
    }

    public static void printInstructions(AbstractInsnNode instructions[], int start, int end) {
        boolean is_first_label = true;
        HashMap<Label, String> label_data = BuildLabelMap(instructions);

        for (int i = start; i < end; ++i) {
            String value = null;
            AbstractInsnNode e = instructions[i];

            switch (e.getOpcode()) {
                case Opcodes.ILOAD:
                case Opcodes.ALOAD:
                case Opcodes.ISTORE:
                case Opcodes.ASTORE:
                    if (e instanceof VarInsnNode) {
                        value = '_' + String.valueOf(((VarInsnNode) e).var);
                    }
                    break;
                case Opcodes.BIPUSH:
                case Opcodes.SIPUSH:
                    value = "  " + String.valueOf(((IntInsnNode) e).operand);
                    break;
                case Opcodes.LDC:
                    if (((LdcInsnNode) e).cst instanceof String) {
                        value = "  \"" + String.valueOf(((LdcInsnNode) e).cst) + "\"";
                    } else {
                        value = "  " + String.valueOf(((LdcInsnNode) e).cst);
                    }
                    break;
                case Opcodes.GOTO:
                case Opcodes.IF_ICMPNE:
                case Opcodes.IF_ICMPEQ:
                case Opcodes.IF_ICMPGE:
                case Opcodes.IF_ICMPGT:
                case Opcodes.IF_ICMPLE:
                case Opcodes.IF_ICMPLT:
                case Opcodes.IF_ACMPNE:
                case Opcodes.IFEQ:
                case Opcodes.IFGE:
                case Opcodes.IFGT:
                case Opcodes.IFLE:
                case Opcodes.IFLT:
                case Opcodes.IFNE:
                case Opcodes.IFNONNULL:
                case Opcodes.IFNULL:
                    value = "  " + label_data.get(((JumpInsnNode)e).label.getLabel());
                    break;
            }

            if (value != null) {
                System.out.println("    " + get(e.getOpcode()) + value);
            } else {
                if (e instanceof LabelNode && e.getOpcode() == -1) {
                    System.out.println((is_first_label ? "" : "}\n\n") + label_data.get(((LabelNode)e).getLabel()) + ":\n{");
                    is_first_label = false;
                } else if (e instanceof MethodInsnNode) {
                    MethodInsnNode m = (MethodInsnNode) e;
                    System.out.println("    " + get(e.getOpcode()) + "   " + m.owner + "/" + m.name + m.desc);
                } else if (e instanceof FieldInsnNode) {
                    FieldInsnNode f = (FieldInsnNode) e;
                    System.out.println("    " + get(e.getOpcode()) + "   " + f.owner + "/" + f.name + " " + f.desc);
                } else {
                    System.out.println("    " + get(e.getOpcode()));
                }
            }
        }
    }

    private static HashMap<Label, String> BuildLabelMap(AbstractInsnNode instructions[]) {
        HashMap<Label, String> data = new HashMap<>();
        for (AbstractInsnNode a : instructions) {
            if (a instanceof LabelNode) {
                Label l = ((LabelNode) a).getLabel();
                if (!data.containsKey(l)) {
                    data.put(l, "L" + data.size());
                }
            } else if (a instanceof JumpInsnNode) {
                Label l = ((JumpInsnNode)a).label.getLabel();
                if (!data.containsKey(l)) {
                    data.put(l, "L" + data.size());
                }
            }
        }
        return data;
    }

    public static String get(int opcode) {
        return code_map.get(opcode);
    }

    private static String getAccess(int access) {
        LinkedHashMap<Integer, String> code_map = new LinkedHashMap<>();
        code_map.put(Opcodes.ACC_PUBLIC, "public");
        code_map.put(Opcodes.ACC_PRIVATE, "private");
        code_map.put(Opcodes.ACC_PROTECTED, "protected");
        code_map.put(Opcodes.ACC_STATIC, "static");
        code_map.put(Opcodes.ACC_FINAL, "final");
        code_map.put(Opcodes.ACC_NATIVE, "native");
        code_map.put(Opcodes.ACC_ABSTRACT, "abstract");

        ArrayList<String> accesses = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : code_map.entrySet()) {
            if ((access & entry.getKey()) != 0) {
                accesses.add(entry.getValue());
            }
        }

        return String.join(" ", accesses);
    }

    static {
        code_map.put(0, "NOP");
        code_map.put(1, "ACONST_NULL");
        code_map.put(2, "ICONST_M1");
        code_map.put(3, "ICONST_0");
        code_map.put(4, "ICONST_1");
        code_map.put(5, "ICONST_2");
        code_map.put(6, "ICONST_3");
        code_map.put(7, "ICONST_4");
        code_map.put(8, "ICONST_5");
        code_map.put(9, "LCONST_0");
        code_map.put(10, "LCONST_1");
        code_map.put(11, "FCONST_0");
        code_map.put(12, "FCONST_1");
        code_map.put(13, "FCONST_2");
        code_map.put(14, "DCONST_0");
        code_map.put(15, "DCONST_1");
        code_map.put(16, "BIPUSH");
        code_map.put(17, "SIPUSH");
        code_map.put(18, "LDC");
        code_map.put(21, "ILOAD");
        code_map.put(22, "LLOAD");
        code_map.put(23, "FLOAD");
        code_map.put(24, "DLOAD");
        code_map.put(25, "ALOAD");
        code_map.put(46, "IALOAD");
        code_map.put(47, "LALOAD");
        code_map.put(48, "FALOAD");
        code_map.put(49, "DALOAD");
        code_map.put(50, "AALOAD");
        code_map.put(51, "BALOAD");
        code_map.put(52, "CALOAD");
        code_map.put(53, "SALOAD");
        code_map.put(54, "ISTORE");
        code_map.put(55, "LSTORE");
        code_map.put(56, "FSTORE");
        code_map.put(57, "DSTORE");
        code_map.put(58, "ASTORE");
        code_map.put(79, "IASTORE");
        code_map.put(80, "LASTORE");
        code_map.put(81, "FASTORE");
        code_map.put(82, "DASTORE");
        code_map.put(83, "AASTORE");
        code_map.put(84, "BASTORE");
        code_map.put(85, "CASTORE");
        code_map.put(86, "SASTORE");
        code_map.put(87, "POP");
        code_map.put(88, "POP2");
        code_map.put(89, "DUP");
        code_map.put(90, "DUP_X1");
        code_map.put(91, "DUP_X2");
        code_map.put(92, "DUP2");
        code_map.put(93, "DUP2_X1");
        code_map.put(94, "DUP2_X2");
        code_map.put(95, "SWAP");
        code_map.put(96, "IADD");
        code_map.put(97, "LADD");
        code_map.put(98, "FADD");
        code_map.put(99, "DADD");
        code_map.put(100, "ISUB");
        code_map.put(101, "LSUB");
        code_map.put(102, "FSUB");
        code_map.put(103, "DSUB");
        code_map.put(104, "IMUL");
        code_map.put(105, "LMUL");
        code_map.put(106, "FMUL");
        code_map.put(107, "DMUL");
        code_map.put(108, "IDIV");
        code_map.put(109, "LDIV");
        code_map.put(110, "FDIV");
        code_map.put(111, "DDIV");
        code_map.put(112, "IREM");
        code_map.put(113, "LREM");
        code_map.put(114, "FREM");
        code_map.put(115, "DREM");
        code_map.put(116, "INEG");
        code_map.put(117, "LNEG");
        code_map.put(118, "FNEG");
        code_map.put(119, "DNEG");
        code_map.put(120, "ISHL");
        code_map.put(121, "LSHL");
        code_map.put(122, "ISHR");
        code_map.put(123, "LSHR");
        code_map.put(124, "IUSHR");
        code_map.put(125, "LUSHR");
        code_map.put(126, "IAND");
        code_map.put(127, "LAND");
        code_map.put(128, "IOR");
        code_map.put(129, "LOR");
        code_map.put(130, "IXOR");
        code_map.put(131, "LXOR");
        code_map.put(132, "IINC");
        code_map.put(133, "I2L");
        code_map.put(134, "I2F");
        code_map.put(135, "I2D");
        code_map.put(136, "L2I");
        code_map.put(137, "L2F");
        code_map.put(138, "L2D");
        code_map.put(139, "F2I");
        code_map.put(140, "F2L");
        code_map.put(141, "F2D");
        code_map.put(142, "D2I");
        code_map.put(143, "D2L");
        code_map.put(144, "D2F");
        code_map.put(145, "I2B");
        code_map.put(146, "I2C");
        code_map.put(147, "I2S");
        code_map.put(148, "LCMP");
        code_map.put(149, "FCMPL");
        code_map.put(150, "FCMPG");
        code_map.put(151, "DCMPL");
        code_map.put(152, "DCMPG");
        code_map.put(153, "IFEQ");
        code_map.put(154, "IFNE");
        code_map.put(155, "IFLT");
        code_map.put(156, "IFGE");
        code_map.put(157, "IFGT");
        code_map.put(158, "IFLE");
        code_map.put(159, "IF_ICMPEQ");
        code_map.put(160, "IF_ICMPNE");
        code_map.put(161, "IF_ICMPLT");
        code_map.put(162, "IF_ICMPGE");
        code_map.put(163, "IF_ICMPGT");
        code_map.put(164, "IF_ICMPLE");
        code_map.put(165, "IF_ACMPEQ");
        code_map.put(166, "IF_ACMPNE");
        code_map.put(167, "GOTO");
        code_map.put(168, "JSR");
        code_map.put(169, "RET");
        code_map.put(170, "TABLESWITCH");
        code_map.put(171, "LOOKUPSWITCH");
        code_map.put(172, "IRETURN");
        code_map.put(173, "LRETURN");
        code_map.put(174, "FRETURN");
        code_map.put(175, "DRETURN");
        code_map.put(176, "ARETURN");
        code_map.put(177, "RETURN");
        code_map.put(178, "GETSTATIC");
        code_map.put(179, "PUTSTATIC");
        code_map.put(180, "GETFIELD");
        code_map.put(181, "PUTFIELD");
        code_map.put(182, "INVOKEVIRTUAL");
        code_map.put(183, "INVOKESPECIAL");
        code_map.put(184, "INVOKESTATIC");
        code_map.put(185, "INVOKEINTERFACE");
        code_map.put(186, "INVOKEDYNAMIC");
        code_map.put(187, "NEW");
        code_map.put(188, "NEWARRAY");
        code_map.put(189, "ANEWARRAY");
        code_map.put(190, "ARRAYLENGTH");
        code_map.put(191, "ATHROW");
        code_map.put(192, "CHECKCAST");
        code_map.put(193, "INSTANCEOF");
        code_map.put(194, "MONITORENTER");
        code_map.put(195, "MONITOREXIT");
        code_map.put(197, "MULTIANEWARRAY");
        code_map.put(198, "IFNULL");
        code_map.put(199, "IFNONNULL");
    }
}