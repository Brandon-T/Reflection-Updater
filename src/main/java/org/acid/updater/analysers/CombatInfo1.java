package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Brandon on 2017-04-30.
 */
public class CombatInfo1 extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode node : nodes) {
            if (!node.superName.equals(Main.get("Node"))) {
                continue;
            }

            int int_count = 0;
            int method_count = 0;
            for (FieldNode field : node.fields) {
                if (field.desc.equals("I") && !hasAccess(field, Opcodes.ACC_STATIC)) {
                    ++int_count;
                }
            }

            for (MethodNode method : node.methods) {
                if (method.name.equals("<init>") && method.desc.equals("(IIII)V")) {
                    ++method_count;
                }
            }

            if (int_count == 4 && method_count == 1) {
                return node;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo classInfo = new ClassInfo("CombatInfo1", node.name);
        classInfo.putField(findHealth(node));
        classInfo.putField(findHealthRatio(node));
        return classInfo;
    }

    private ClassField findHealth(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (m.instructions.get(i + 1) instanceof VarInsnNode && ((VarInsnNode) m.instructions.get(i + 1)).var == 3) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Health", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Health");
    }

    private ClassField findHealthRatio(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (m.instructions.get(i + 1) instanceof VarInsnNode && ((VarInsnNode) m.instructions.get(i + 1)).var == 2) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("HealthRatio", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("HealthRatio");
    }
}
