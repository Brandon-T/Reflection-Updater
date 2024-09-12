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

public class Varps extends Analyser {

    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object") || !n.interfaces.isEmpty()) {
                continue;
            }

            int int_arrays = 0;
            int method_count = 0;

            for (FieldNode f : n.fields) {
                if (f.desc.equals("[I") && hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++int_arrays;
                }
            }

            for (MethodNode m : n.methods) {
                if (m.name.equals("<clinit>")) {
                    int i = new Finder(m).findPattern(new int[]{Opcodes.BIPUSH, Opcodes.NEWARRAY, Opcodes.PUTSTATIC});
                    if (i != -1) {
                        ++method_count;
                        break;
                    }
                }
            }

            if (int_arrays == 3 && method_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Varps", node.name);
        info.putField(findMasks(node));
        info.putField(findMain(node));
        return info;
    }

    private ClassField findMasks(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.BIPUSH, Opcodes.NEWARRAY, Opcodes.PUTSTATIC};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                    return new ClassField("Masks", f.name, f.desc);
                }
            }
        }
        return new ClassField("Masks");
    }

    private ClassField findMain(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.SIPUSH, Opcodes.NEWARRAY, Opcodes.PUTSTATIC, Opcodes.RETURN};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                    return new ClassField("Main", f.name, f.desc);
                }
            }
        }
        return new ClassField("Main");
    }
}
