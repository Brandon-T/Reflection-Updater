package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public class PacketBuffer extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Buffer"))) {
                continue;
            }

            for (MethodNode m : n.methods) {
                if (m.name.equals("<init>") && m.desc.equals("(I)V")) {
                    if (new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.INVOKESPECIAL, Opcodes.RETURN}) != -1) {
                        return n;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("PacketBuffer", node);
        info.putField(findNewIsaacCipher(node));
        return info;
    }

    private ClassField findNewIsaacCipher(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("([I)V")) {
                return new ClassField("*newIsaacCipher", m.name, m.desc);
            }
        }

        return new ClassField("newIsaacCipher");
    }
}
