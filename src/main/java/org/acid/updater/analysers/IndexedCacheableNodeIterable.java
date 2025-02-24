package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Collection;

public class IndexedCacheableNodeIterable extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.interfaces.contains("java/lang/Iterable")) {
                continue;
            }

            int int_count = 0;
            int node_count = 0;
            int node_arr_count = 0;

            for (FieldNode f: n.fields) {
                if (f.desc.equals("I")) {
                    int_count++;
                } else if (f.desc.equals(String.format("L%s;", Main.get("IndexedCacheableNode")))) {
                    node_count++;
                } else if (f.desc.equals(String.format("[L%s;", Main.get("IndexedCacheableNode")))) {
                    node_arr_count++;
                }
            }

            if (int_count == 1 && node_count == 1 && node_arr_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("IndexedCacheableNodeIterable", node);
        info.putField(findSize(node));
        info.putField(findCurrentNode(node));
        info.putField(findNodes(node));
        info.putField(findIterator(node));
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

    private ClassField findCurrentNode(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("IndexedCacheableNode")))) {
                return new ClassField("CurrentNode", f.name, f.desc);
            }
        }
        return new ClassField("CurrentNode");
    }

    private ClassField findNodes(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[L%s;", Main.get("IndexedCacheableNode")))) {
                return new ClassField("Nodes", f.name, f.desc);
            }
        }
        return new ClassField("Nodes");
    }

    private ClassField findIterator(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (hasAccess(f, Opcodes.ACC_FINAL) && !f.desc.equals(String.format("L%s;", Main.get("IndexedCacheableNode")))) {
                return new ClassField("Iterator", f.name, f.desc);
            }
        }
        return new ClassField("Iterator");
    }
}
