package acid.analysers;

import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-21.
 */
public class CollisionMap extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object")) {
                continue;
            }

            int method_count = 0, con_count = 0;
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIIZ)V")) {
                    ++method_count;
                } else if (m.name.equals("<init>") && m.desc.equals("(II)V")) {
                    ++con_count;
                }
            }

            if (con_count > 0 && method_count > 0) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("CollisionMap", node.name);
        info.putField(findField(node, "Width", 1));
        info.putField(findField(node, "Height", 2));
        info.putField(findAdjacency(node));
        info.putField(findResetMethod(node));
        return info;
    }

    private ClassField findField(ClassNode node, String fieldName, int index) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("(II)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                    if (((VarInsnNode)m.instructions.get(i + 1)).var == index) {
                        long multi = (int) ((LdcInsnNode)m.instructions.get(i + 2)).cst;
                        return new ClassField(fieldName, f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField(fieldName);
    }

    private ClassField findAdjacency(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.MULTIANEWARRAY, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("(II)V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    return new ClassField("Adjacency", f.name, f.desc);
                }
            }
        }
        return new ClassField("Adjacency");
    }

    private ClassField findResetMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("(II)V")) {
                int i = new Finder(m).findNext(0, Opcodes.INVOKEVIRTUAL);
                while (i != -1) {
                    MethodInsnNode n = (MethodInsnNode)m.instructions.get(i);
                    if (n.owner.equals(node.name)) {
                        return new ClassField("*Reset", n.name, n.desc);
                    }
                    i = new Finder(m).findNext(i + 1, Opcodes.INVOKEVIRTUAL);
                }
            }
        }
        return new ClassField("*Reset");
    }
}