package org.acid.updater.analysers;

import org.acid.updater.Main;
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
 * Created by Brandon on 2014-12-23.
 */
public class Cache extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object") && !hasAccess(n, Opcodes.ACC_FINAL)) { //Final added August 20th, 2019.
                continue;
            }

            int cnode_count = 0, htable_count = 0, queue_count = 0;
            for (FieldNode f : n.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("L%s;", Main.get("CacheableNode")))) {
                    ++cnode_count;
                } else if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("L%s;", Main.get("IterableHashTable")))) {
                    ++htable_count;
                } else if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("L%s;", Main.get("Queue")))) {
                    ++queue_count;
                }
            }

            if (cnode_count >= 1 && htable_count >= 1 && queue_count >= 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Cache", node.name);
        info.putField(findHashTable(node));
        info.putField(findQueue(node));
        info.putField(findRemaining(node));
        info.putField(findCapacity(node));
        return info;
    }

    private ClassField findHashTable(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("L%s;", Main.get("IterableHashTable")))) {
                return new ClassField("HashTable", f.name, f.desc);
            }
        }
        return new ClassField("HashTable");
    }

    private ClassField findQueue(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("L%s;", Main.get("Queue")))) {
                return new ClassField("Queue", f.name, f.desc);
            }
        }
        return new ClassField("Queue");
    }

    private ClassField findRemaining(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                    return new ClassField("Remaining", f.name, f.desc);
                }
            }
        }
        return new ClassField("Remaining");
    }

    private ClassField findCapacity(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                    return new ClassField("Capacity", f.name, f.desc);
                }
            }
        }
        return new ClassField("Capacity");
    }
}
