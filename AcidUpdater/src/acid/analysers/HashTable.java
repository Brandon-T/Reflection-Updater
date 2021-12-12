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
public class HashTable extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object") || !hasAccess(n, Opcodes.ACC_FINAL)) {
                continue;
            }

            if (n.interfaces.contains("java/lang/Iterable")) {
                continue;
            }

            int arr_count = 0, node_count = 0, int_count = 0;
            for (FieldNode f : n.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("[L%s;", Main.get("Node")))) {
                    ++arr_count;
                } else if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("L%s;", Main.get("Node")))) {
                    ++node_count;
                } else if (f.desc.equals("I")) {
                    ++int_count;
                }
            }

            if (arr_count == 1 && node_count == 2 && int_count >= 2) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("HashTable", node.name);
        info.putField(findHead(node));
        info.putField(findTail(node));
        info.putField(findCache(node));
        info.putField(findIndex(node));
        info.putField(findCapacity(node));
        return info;
    }

    private ClassField findHead(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(J)L%s;", Main.get("Node")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                    return new ClassField("Head", f.name, f.desc);
                }
            }
        }
        return new ClassField("Head");
    }

    private ClassField findTail(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Node")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 3);
                    return new ClassField("Tail", f.name, f.desc);
                }
            }
        }
        return new ClassField("Tail");
    }

    private ClassField findCache(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("[L%s;", Main.get("Node")))) {
                return new ClassField("Cache|Buckets", f.name, f.desc);
            }
        }
        return new ClassField("Cache|Buckets");
    }

    private ClassField findIndex(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IFLE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Node")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    return new ClassField("Index", f.name, f.desc);
                }
            }
        }
        return new ClassField("Index");
    }

    private ClassField findCapacity(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 1)).var == 1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                        return new ClassField("Capacity", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Capacity");
    }
}
