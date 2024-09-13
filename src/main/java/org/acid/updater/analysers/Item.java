package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.DeprecatedFinder;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Brandon on 2014-12-14.
 */
public class Item extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Renderable")) || !n.interfaces.contains("net/runelite/api/TileItem")) {
                continue;
            }

            int int_count = 0;
            int int_array_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("I") && !hasAccess(f, Opcodes.ACC_STATIC) && !hasAccess(f, Opcodes.ACC_FINAL)) {
                    ++int_count;
                }

                if (f.desc.equals("[I") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++int_array_count;
                }
            }

            int method_count = 0;
            int constructor_count = 0;
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                    ++method_count;
                }

                if (m.name.equals("<init>") && m.desc.equals("()V")) {
                    ++constructor_count;
                }
            }

            if (int_count >= 2 && int_array_count == 0 && method_count >= 1 && constructor_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Item", node.name);
        info.putField(findID(node));
        info.putField(findQuantity(node));
        return info;
    }

    private ClassField findID(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Finder.OPTIONAL, Opcodes.IMUL, Finder.OPTIONAL, Opcodes.INVOKESTATIC};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                int i = new DeprecatedFinder(m).findPattern(pattern, 0, true);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                    return new ClassField("ID", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("ID");
    }

    private ClassField findQuantity(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Finder.OPTIONAL, Opcodes.INVOKEVIRTUAL};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                    return new ClassField("Quantity", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("Quantity");
    }
}
