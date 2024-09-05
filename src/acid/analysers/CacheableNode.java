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
public class CacheableNode extends Analyser {

    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Node"))) {
                continue;
            }

            int node_count = 0;
            for (FieldNode f : n.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC)) {
                    if (f.desc.equals(String.format("L%s;", n.name)) && (++node_count == 2)) {
                        return n;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("CacheableNode", node.name);
        info.putField(findNext(node));
        info.putField(findPrev(node, info.getField("Next")));
        return info;
    }

    private ClassField findPrev(ClassNode node, ClassField next) {
        for (FieldNode n : node.fields) {
            if (!n.name.equals(next.getName()) && n.desc.equals(next.getDesc())) {
                return new ClassField("Prev", n.name, n.desc);
            }
        }
        return new ClassField("Prev");
    }

    private ClassField findNext(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (!m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IFNONNULL});
                if (m.instructions.get(i + 1) instanceof FieldInsnNode) {
                    FieldInsnNode next = (FieldInsnNode) m.instructions.get(i + 1);
                    return new ClassField("Next", next.name, next.desc);
                }
            }
        }
        return new ClassField("Next");
    }
}
