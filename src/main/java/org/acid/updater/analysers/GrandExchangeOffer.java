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
 * Created by Brandon on 2015-01-13.
 */
public class GrandExchangeOffer extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object")) {
                continue;
            }

            int init_count = 0;
            for (MethodNode m : n.methods) {
                if (m.name.equals("<init>") && m.desc.equals(String.format("(L%s;Z)V", Main.get("Buffer")))) {
                    ++init_count;
                }
            }

            int int_count = 0, byte_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("I") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++int_count;
                } else if (f.desc.equals("B") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++byte_count;
                }
            }

            if (init_count == 1 && byte_count == 1 && int_count == 5) {
                return n;
            }
        }

        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("GrandExchangeOffer", node.name);
        info.putField(findStatus(node));
        info.putField(findItemID(node));
        info.putField(findPrice(node));
        info.putField(findQuantity(node));
        info.putField(findTransferred(node));
        info.putField(findSpent(node));
        info.putField(findQueryIDs(node));
        return info;
    }

    private ClassField findStatus(ClassNode node) {
        return findFieldInfo(node, "Status", 0);
    }

    private ClassField findItemID(ClassNode node) {
        return findFieldInfo(node, "ItemID", 1);
    }

    private ClassField findPrice(ClassNode node) {
        return findFieldInfo(node, "Price", 2);
    }

    private ClassField findQuantity(ClassNode node) {
        return findFieldInfo(node, "Quantity", 3);
    }

    private ClassField findTransferred(ClassNode node) {
        return findFieldInfo(node, "Transferred", 4);
    }

    private ClassField findSpent(ClassNode node) {
        return findFieldInfo(node, "Spent", 5);
    }

    private ClassField findQueryIDs(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ACONST_NULL, Opcodes.PUTSTATIC};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(Ljava/lang/String;)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (f.desc.equals("[S")) {
                            return new ClassField("QueryIDs", f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        return new ClassField("QueryIDs");
    }

    private ClassField findFieldInfo(ClassNode node, String name, int index) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals(String.format("(L%s;Z)V", Main.get("Buffer")))) {
                int i = 0;
                int j = new Finder(m).findNext(0, Opcodes.PUTFIELD, false);
                while (j != -1) {
                    if (i == index) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField(name, f.name, f.desc, multi);
                    }
                    j = new Finder(m).findNext(j + 1, Opcodes.PUTFIELD, false);
                    ++i;
                }
            }
        }
        return new ClassField(name);
    }
}
