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

public class CombatInfoList extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode node : nodes) {
            if (!node.superName.equals("java/lang/Object") || !node.interfaces.contains("java/lang/Iterable")) {
                continue;
            }

            int node_count = 0;
            String nodeField = Main.get("Node");

            for (FieldNode field : node.fields) {
                if (field.desc.equals(String.format("L%s;", nodeField))) {
                    ++node_count;
                }
            }

            if (node_count == 2 && node.fields.size() == 2) {
                return node;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo classInfo = new ClassInfo("CombatInfoList", node);
        classInfo.putField(findHead(node));
        classInfo.putField(findCurrent(node, classInfo.getField("Head")));
        return classInfo;
    }

    private ClassField findHead(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Node")))) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.PUTFIELD});
                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                return new ClassField("Head", f.name, f.desc);
            }
        }
        return new ClassField("Head");
    }

    private ClassField findCurrent(ClassNode node, ClassField head) {
        for (FieldNode n : node.fields) {
            if (!n.name.equals(head.getName()) && n.desc.equals(head.getDesc())) {
                return new ClassField("Current", n.name, n.desc);
            }
        }
        return new ClassField("Current");
    }
}
