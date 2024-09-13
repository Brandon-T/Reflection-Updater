package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Brandon on 2017-04-29.
 */
public class CombatInfoHolder extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode node : nodes) {
            if (!node.superName.equals(Main.get("Node"))) {
                continue;
            }

            int combatInfo2_count = 0;
            int combatInfo1_MethodCount = 0;

            for (FieldNode field : node.fields) {
                if (!hasAccess(field, Opcodes.ACC_STATIC) && field.desc.equals(String.format("L%s;", Main.get("CombatInfo2")))) {
                    ++combatInfo2_count;
                }
            }

            for (MethodNode method : node.methods) {
                if (!hasAccess(method, Opcodes.ACC_STATIC) && method.desc.equals(String.format("(I)L%s;", Main.get("CombatInfo1")))) {
                    ++combatInfo1_MethodCount;
                }
            }

            if (combatInfo2_count == 1 && combatInfo1_MethodCount >= 1) {
                return node;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo classInfo = new ClassInfo("CombatInfoHolder", node.name);
        classInfo.putField(findLinkedList(node));
        classInfo.putField(findHealthBarDefinition(node));
        return classInfo;
    }

    private ClassField findLinkedList(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("CombatInfoList")))) {
                return new ClassField("CombatInfoList", f.name, f.desc);
            }
        }
        return new ClassField("CombatInfoList");
    }

    private ClassField findHealthBarDefinition(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("CombatInfo2")))) {
                return new ClassField("CombatInfo2", f.name, f.desc);
            }
        }
        return new ClassField("CombatInfo2");
    }
}
