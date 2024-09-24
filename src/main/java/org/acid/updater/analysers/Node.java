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

/**
 * Created by Brandon on 2014-12-06.
 */
public class Node extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object")) {
                continue;
            }

            int long_count = 0, node_count = 0;
            for (FieldNode f : n.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC)) {
                    if (f.desc.equals(String.format("L%s;", n.name))) {
                        ++node_count;
                    } else if (f.desc.equals("J")) {
                        ++long_count;
                    }
                }
            }

            if (long_count == 1 && node_count == 2) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Node", node);
        info.putField(findID(node));
        info.putField(findPrev(node));
        info.putField(findNext(node, info.getField("Prev")));
        return info;
    }


    private ClassField findID(ClassNode node) {
        for (FieldNode f : node.fields) {
            if ((f.access & Opcodes.ACC_STATIC) == 0) {
                if (f.desc.equals("J")) {
                    return new ClassField("UID", f.name, f.desc);
                }
            }
        }
        return new ClassField("UID");
    }

    private ClassField findNext(ClassNode node, ClassField next) {
        for (FieldNode n : node.fields) {
            if (!n.name.equals(next.getName()) && n.desc.equals(next.getDesc())) {
                return new ClassField("Next", n.name, n.desc);
            }
        }
        return new ClassField("Next");
    }

    private ClassField findPrev(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (!m.name.equals("<init>")) { //!m.name.equals("<init>") && m.desc.equals("()V")
                int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IFNONNULL});
                if (i != -1) {
                    FieldInsnNode next = (FieldInsnNode) m.instructions.get(i + 1);
                    return new ClassField("Prev", next.name, next.desc);
                }
            }
        }
        return new ClassField("Prev");
    }
}
