package org.acid.updater.analysers;

import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public class NameInfo extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object")) {
                continue;
            }

            if (!n.interfaces.contains("java/lang/Comparable")) {
                continue;
            }

            int stringCount = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("Ljava/lang/String;")) {
                    ++stringCount;
                }
            }

            if (stringCount == 2) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("NameInfo", node);
        info.putField(findName(node));
        info.putField(findDecodedName(node));
        return info;
    }

    private ClassField findName(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 0);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    return new ClassField("Name", f.name, f.desc);
                }
            }
        }

        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ARETURN};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()Ljava/lang/String;")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    return new ClassField("Name", f.name, f.desc);
                }
            }
        }

        pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                    return new ClassField("Name", f.name, f.desc);
                }
            }
        }

        return new ClassField("Name");
    }

    private ClassField findDecodedName(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 1);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    return new ClassField("DecodedName", f.name, f.desc);
                }
            }
        }

        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.INVOKESTATIC, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                    return new ClassField("DecodedName", f.name, f.desc);
                }
            }
        }

        return new ClassField("DecodedName");
    }
}
