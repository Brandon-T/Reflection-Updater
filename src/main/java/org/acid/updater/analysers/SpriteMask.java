package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public class SpriteMask extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode node : nodes) {
            if (!node.superName.equals(Main.get("CacheableNode"))) {
                continue;
            }

            int method_count = 0, contains_method_count = 0;
            for (MethodNode m : node.methods) {
                if (m.name.equals("<init>") && m.desc.equals("(II[I[I)V")) {
                    ++method_count;
                } else if (m.desc.equals("(II)Z") && !hasAccess(m, Opcodes.ACC_STATIC)) {
                    ++contains_method_count;
                }
            }

            if (method_count == 1 && contains_method_count >= 1) {
                return node;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("SpriteMask", node.name);
        return info;
    }
}
