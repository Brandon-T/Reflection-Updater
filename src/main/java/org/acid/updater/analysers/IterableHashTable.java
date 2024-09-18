package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

public class IterableHashTable extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object") || !hasAccess(n, Opcodes.ACC_FINAL)) {
                continue;
            }

            if (!n.interfaces.contains("java/lang/Iterable")) {
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
        ClassInfo info = new ClassInfo("IterableHashTable", node);
        info.putField(findHead(node));
        info.putField(findTail(node));
        info.putField(findCache(node));
        info.putField(findIndex(node));
        info.putField(findSize(node));
        return info;
    }

    private ClassField findHead(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(J)L%s;", Main.get("Node")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
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
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                    return new ClassField("Tail", f.name, f.desc);
                }
            }
        }
        return new ClassField("Tail");
    }

    private ClassField findCache(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("[L%s;", Main.get("Node")))) {
                return new ClassField("Buckets", f.name, f.desc);
            }
        }
        return new ClassField("Buckets");
    }

    private ClassField findIndex(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IFLE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Node")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    return new ClassField("Index", f.name, f.desc);
                }
            }
        }
        return new ClassField("Index");
    }

    private ClassField findSize(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 1)).var == 1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        return new ClassField("Size", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Size");
    }
}

