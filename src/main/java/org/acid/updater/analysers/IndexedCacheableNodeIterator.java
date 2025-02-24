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

public class IndexedCacheableNodeIterator extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.interfaces.contains("java/util/Iterator")) {
                continue;
            }

            int int_count = 0;
            int node_count = 0;
            int iterable_count = 0;

            for (FieldNode f: n.fields) {
                if (f.desc.equals("I")) {
                    int_count++;
                } else if (f.desc.equals(String.format("L%s;", Main.get("IndexedCacheableNode")))) {
                    node_count++;
                } else if (f.desc.equals(String.format("L%s;", Main.get("IndexedCacheableNodeIterable")))) {
                    iterable_count++;
                }
            }

            if (int_count == 1 && node_count == 2 && iterable_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("IndexedCacheableNodeIterator", node);
        info.putField(findSize(node));
        var lastVisitedNode = findLastVisitedNode(node);
        info.putField(lastVisitedNode);
        info.putField(findCurrentNode(node, lastVisitedNode));
        info.putField(findIterable(node));
        return info;
    }

    private ClassField findSize(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("I")) {
                return new ClassField("Size", f.name, f.desc);
            }
        }
        return new ClassField("Size");
    }

    private ClassField findCurrentNode(ClassNode node, ClassField lastVisitedNode) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("IndexedCacheableNode"))) &&
                !f.name.equals(lastVisitedNode.getName())) {
                return new ClassField("CurrentNode", f.name, f.desc);
            }
        }
        return new ClassField("CurrentNode");
    }

    private ClassField findLastVisitedNode(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (!m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.ACONST_NULL, Opcodes.PUTFIELD});
                if (m.instructions.get(i + 2) instanceof FieldInsnNode next) {
                    if (next.desc.equals(String.format("L%s;", Main.get("IndexedCacheableNode")))) {
                        return new ClassField("LastVisitedNode", next.name, next.desc);
                    }
                }
            }
        }
        return new ClassField("LastVisitedNode");
    }

    private ClassField findIterable(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("IndexedCacheableNodeIterable")))) {
                return new ClassField("Iterable", f.name, f.desc);
            }
        }
        return new ClassField("Iterable");
    }
}