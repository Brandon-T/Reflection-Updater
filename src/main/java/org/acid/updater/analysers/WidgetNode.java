package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Brandon on 2014-12-15.
 */
public class WidgetNode extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Node"))) {
                continue;
            }

            int int_count = 0, bool_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("I") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++int_count;
                } else if (f.desc.equals("Z") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++bool_count;
                }
            }

            if (int_count == 2 && bool_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("WidgetNode", node.name);
        info.putField(findID(node));
        return info;
    }

    private ClassField findID(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;Z)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (((VarInsnNode) m.instructions.get(i + 4)).var == 3) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("ID", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("ID");
    }
}
