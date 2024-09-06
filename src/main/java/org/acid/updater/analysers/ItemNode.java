package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

public class ItemNode extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Node"))) {
                continue;
            }

            int int_arr_count = 0, hash_table_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("[I") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++int_arr_count;
                } else if (f.desc.equals(String.format("L%s;", Main.get("HashTable"))) && hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++hash_table_count;
                }
            }

            if (int_arr_count == 2 && hash_table_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("ItemNode", node.name);
        info.putField(findItemIDs(node));
        info.putField(findQuantities(node));
        info.putField(findCache(node));
        return info;
    }

    private ClassField findItemIDs(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ICONST_M1, Opcodes.IASTORE, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                    return new ClassField("ItemIDs", f.name, f.desc);
                }
            }
        }
        return new ClassField("ItemIDs");
    }

    private ClassField findQuantities(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ICONST_0, Opcodes.IASTORE, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                    return new ClassField("ItemQuantities", f.name, f.desc);
                }
            }
        }
        return new ClassField("ItemQuantities");
    }

    private ClassField findCache(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("HashTable")))) {
                return new ClassField("Cache", f.name, f.desc);
            }
        }
        return new ClassField("Cache");
    }
}