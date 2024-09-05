package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldInsnNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-07.
 */
public class Animable extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("CacheableNode")) || !hasAccess(n, Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC)) {
                continue;
            }

            int field_count = 0, method_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("I") && !hasAccess(f, Opcodes.ACC_FINAL | Opcodes.ACC_STATIC)) {
                    ++field_count;
                }
            }

            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIIIIIIIJ)V")) {
                    ++method_count;
                } else if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.contains(Main.get("Buffer"))) {
                    method_count = 0;
                    break;
                }
            }

            if (field_count == 1 && method_count >= 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Animable", node.name);
        info.putField(findModelHeight(node));
        return info;
    }

    private ClassField findModelHeight(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Finder.WILDCARD, Opcodes.PUTFIELD});
                FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                long multi = Main.findMultiplier(f.owner, f.name);
                return new ClassField("ModelHeight", f.name, f.desc, multi);
            }
        }
        return new ClassField("ModelHeight");
    }
}
