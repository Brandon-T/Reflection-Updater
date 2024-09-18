package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.DeprecatedFinder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

public class PlayerUpdateManager extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object")) {
                continue;
            }

            int buffer_field_count = 0;
            int string_array_field_count = 0;
            int bool_array_field_count = 0;
            for (FieldNode f : n.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("L%s;", Main.get("Buffer")))) {
                    ++buffer_field_count;
                }
                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[Ljava/lang/String;")) {
                    ++string_array_field_count;
                }
                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[Z")) {
                    ++bool_array_field_count;
                }
            }
            if (buffer_field_count == 1 && string_array_field_count == 1 && bool_array_field_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("PlayerUpdateManager", node);
        info.putField(findPlayerCount(node));
        info.putField(findPlayerIndices(node));
        return info;
    }

    private ClassField findPlayerCount(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.DUP, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IADD, Opcodes.DUP_X1, Opcodes.PUTFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_1, Opcodes.ISUB, Opcodes.ILOAD, Opcodes.IASTORE, Opcodes.ALOAD};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;)V", Main.get("PacketBuffer")))) {
                int i = new DeprecatedFinder(m).findPattern(pattern, 0, false);
                while (i != -1) {
                    FieldInsnNode getCall = (FieldInsnNode) m.instructions.get(i + 4);
                    FieldInsnNode putCall = (FieldInsnNode) m.instructions.get(i + 8);
                    if (getCall.desc.equals("I") && getCall.owner.equals(putCall.owner) && getCall.name.equals(putCall.name) && getCall.desc.equals(putCall.desc)) {
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 9)).cst;
                        return new ClassField("PlayerCount", getCall.name, getCall.desc, multi);
                    }
                    i = new DeprecatedFinder(m).findPattern(pattern, i + 1, false);
                }
            }
        }
        return new ClassField("PlayerCount");
    }

    private ClassField findPlayerIndices(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.DUP, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IADD, Opcodes.DUP_X1, Opcodes.PUTFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_1, Opcodes.ISUB, Opcodes.ILOAD, Opcodes.IASTORE, Opcodes.ALOAD};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;)V", Main.get("PacketBuffer")))) {
                int i = new DeprecatedFinder(m).findPattern(pattern, 0, false);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    if (f.desc.equals("[I")) {
                        return new ClassField("PlayerIndices", f.name, f.desc);
                    }
                    i = new DeprecatedFinder(m).findPattern(pattern, i + 1, false);
                }
            }
        }
        return new ClassField("PlayerIndices");
    }

}