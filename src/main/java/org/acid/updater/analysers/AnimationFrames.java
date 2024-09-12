package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public class AnimationFrames extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("CacheableNode")) || !hasAccess(n, Opcodes.ACC_PUBLIC) || hasAccess(n, Opcodes.ACC_ABSTRACT)) {
                continue;
            }

            int methodCount = 0;
            int fieldCount = 0;

            for (FieldNode f : n.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL) && Type.getType(f.desc).getSort() == Type.ARRAY) {
                    ++fieldCount;
                }
            }

            for (MethodNode m : n.methods) {
                if (m.desc.equals("(I)Z") && hasAccess(m, Opcodes.ACC_PUBLIC) && !hasAccess(m, Opcodes.ACC_STATIC)) {
                    ++methodCount;
                }
            }

            if (fieldCount == 1 && methodCount == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("AnimationFrames", node.name);
        info.putField(findFrames(node));
        return info;
    }

    private ClassField findFrames(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)Z")) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD});
                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                return new ClassField("Frames", f.name, f.desc);
            }
        }
        return new ClassField("Frames");
    }
}
