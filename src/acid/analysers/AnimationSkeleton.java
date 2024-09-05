package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.Collection;

public class AnimationSkeleton extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Node")) && !hasAccess(n, Opcodes.ACC_FINAL)) {
                continue;
            }

            int int_count = 0;
            int int_array_count = 0;
            int int_2d_array_count = 0;

            for (FieldNode f : n.fields) {
                if (f.desc.equals("I") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++int_count;
                }

                if (f.desc.equals("[I") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++int_array_count;
                }

                if (f.desc.equals("[[I") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++int_2d_array_count;
                }
            }

            if (int_count == 2 && int_array_count == 1 && int_2d_array_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("AnimationSkeleton", node.name);
        info.putField(findId(node));
        info.putField(findTransformationCount(node));
        info.putField(findTransformationTypes(node));
        info.putField(findTransformations(node));
        return info;
    }

    private ClassField findId(ClassNode node) {
        int[] pattern = {Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("(I[B)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((FieldInsnNode)m.instructions.get(i + 4)).desc.equals("I") && ((VarInsnNode)m.instructions.get(i + 1)).var == 1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 4);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                        return new ClassField("ID", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("ID");
    }

    private ClassField findTransformationCount(ClassNode node) {
        int[] pattern = {Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("(I[B)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((FieldInsnNode)m.instructions.get(i + 5)).desc.equals("I") && ((VarInsnNode)m.instructions.get(i + 1)).var == 3) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 5);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 3)).cst;
                        return new ClassField("TransformationCount", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("TransformationCount");
    }

    private ClassField findTransformationTypes(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[I") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                return new ClassField("TransformationTypes", f.name, f.desc);
            }
        }
        return new ClassField("TransformationTypes");
    }

    private ClassField findTransformations(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[[I") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                return new ClassField("Transformations", f.name, f.desc);
            }
        }
        return new ClassField("Transformations");
    }
}
