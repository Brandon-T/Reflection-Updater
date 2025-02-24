package org.acid.updater.analysers;

import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

public class PacketWriter extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode node : nodes) {
            if (!node.superName.equals("java/lang/Object")) {
                continue;
            }

            final int[] pattern = new int[]{Opcodes.LDC, Opcodes.INVOKESPECIAL};
            for (MethodNode m : node.methods) {
                if (m.name.equals("<init>") && m.desc.equals("()V")) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        int value = (int) ((LdcInsnNode) m.instructions.get(i)).cst;
                        if (value == 40000) {
                            return node;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("PacketWriter", node);
        info.putField(findStream(node));
        info.putField(findPacketBuffer(node));
        return info;
    }

    ClassField findStream(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.SIPUSH, Opcodes.INVOKESPECIAL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    int value = ((IntInsnNode) m.instructions.get(i)).operand;
                    if (value == 5000) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        return new ClassField("Buffer", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Buffer");
    }

    ClassField findPacketBuffer(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.LDC, Opcodes.INVOKESPECIAL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    int value = (int) ((LdcInsnNode) m.instructions.get(i)).cst;
                    if (value == 40000) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        return new ClassField("PacketBuffer", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("PacketBuffer");
    }
}