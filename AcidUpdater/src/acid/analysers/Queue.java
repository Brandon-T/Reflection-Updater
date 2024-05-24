package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-23.
 */
public class Queue extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object") || !n.interfaces.contains("java/lang/Iterable")) {
                continue;
            }

            int cnode_count = 0;
            int other_fields_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals(String.format("L%s;", Main.get("CacheableNode"))) && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++cnode_count;
                } else {
                    ++other_fields_count;
                }
            }

            int mnode_count = 0;
            for (MethodNode m : n.methods) {
                if (m.desc.equals("()V") && m.name.equals("<init>")) {
                    mnode_count += 1;
                }
            }

            if (other_fields_count == 0 && cnode_count >= 1 && mnode_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Queue", node.name);
        info.putField(findHead(node));
        info.putField(findInsertHead(node));
        info.putField(findInsertTail(node));
        return info;
    }

    private ClassField findHead(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("CacheableNode")))) {
                return new ClassField("Head", f.name, f.desc);
            }
        }
        return new ClassField("Head");
    }

    private ClassField findInsertHead(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("CacheableNode")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 3);
                    if (f.name.equals(Main.getInfo("CacheableNode").getField("Next").getName())) {
                        return new ClassField("*InsertHead", m.name, m.desc);
                    }
                }
            }
        }
        return new ClassField("*InsertHead");
    }

    private ClassField findInsertTail(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("CacheableNode")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 3);
                    if (f.name.equals(Main.getInfo("CacheableNode").getField("Prev").getName())) {
                        return new ClassField("*InsertTail", m.name, m.desc);
                    }
                }
            }
        }
        return new ClassField("*InsertTail");
    }
}
