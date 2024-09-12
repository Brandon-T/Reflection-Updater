package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

public class ObjectDefinition extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("CacheableNode"))) {
                continue;
            }

            int short_arr = 0;
            int caches = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("[S") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++short_arr;
                }

                if (f.desc.equals(String.format("L%s;", Main.get("Cache"))) && hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++caches;
                }
            }

            if (short_arr >= 3 && caches >= 3) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("ObjectDefinition", node.name);
        info.putField(findID(node));
        info.putField(new ClassField("AnimationID"));
        info.putField(findCache(node));
        info.putField(findModelCache(node));
        info.putField(findModelIDs(node));
        info.putField(findModels(node));
        info.putField(findName(node));
        info.putField(findActions(node));
        info.putField(findTransformations(node));
        info.putField(findTransformationVarbit(node));
        info.putField(findTransformationVarp(node));
        info.putField(findGetModelMethod(node));

        info.setField(findAnimationID(node, info.getField("TransformationVarp")));
        return info;
    }

    private ClassField findID(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.BIPUSH, Opcodes.ISHL};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(II[[IIII)L%s;", Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((IntInsnNode) m.instructions.get(i + 4)).operand == 10) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("ID", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("ID");
    }

    private ClassField findAnimationID(ClassNode node, ClassField transformationVarp) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.LDC};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;I)V", Main.get("Buffer")))) {
                int i = new Finder(m).findPattern(pattern, 0, false);
                while (i != -1) {
                    if ((int) ((LdcInsnNode) m.instructions.get(i + 4)).cst == 65535) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (!f.name.equals(transformationVarp.getName())) {
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("AnimationID", f.name, f.desc, multi);
                        }
                    }
                    i = new Finder(m).findPattern(pattern, i + 1, false);
                }
            }
        }
        return new ClassField("AnimationID");
    }

    private ClassField findCache(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.NEW, Opcodes.DUP, Opcodes.SIPUSH, Opcodes.INVOKESPECIAL, Opcodes.PUTSTATIC};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("()V") && m.name.equals("<clinit>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((IntInsnNode) m.instructions.get(i + 2)).operand == 4096) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                        return new ClassField("DefinitionCache", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("DefinitionCache");
    }

    private ClassField findModelCache(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.LLOAD, Opcodes.INVOKEVIRTUAL};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(II[[IIIIL%s;I)L%s;", Main.get("AnimationSequence"), Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    return new ClassField("ModelCache", f.name, f.desc);
                }
            }
        }
        return new ClassField("ModelCache");
    }

    private ClassField findModelIDs(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()Z")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((FieldInsnNode) m.instructions.get(i + 1)).desc.equals("[I")) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("ModelIDs", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("ModelIDs");
    }

    private ClassField findModels(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Finder.WILDCARD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(II[[IIII)L%s;", Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    int opCode = m.instructions.get(i + 2).getOpcode();
                    if (opCode == Opcodes.IFNULL || opCode == Opcodes.IFNONNULL || opCode == Opcodes.ACONST_NULL) {
                        if (((FieldInsnNode) m.instructions.get(i + 1)).desc.equals("[I")) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("Models", f.name, f.desc, multi);
                        }
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Models");
    }

    private ClassField findName(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETSTATIC, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((FieldInsnNode) m.instructions.get(i + 2)).desc.equals("Ljava/lang/String;")) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Name", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Name");
    }

    private ClassField findActions(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ICONST_5, Opcodes.ANEWARRAY, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((FieldInsnNode) m.instructions.get(i + 2)).desc.equals("[Ljava/lang/String;")) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Actions", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Actions");
    }

    private ClassField findTransformations(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ARRAYLENGTH};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", node.name))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    FieldInsnNode g = (FieldInsnNode) m.instructions.get(i + 3);
                    if (f.name.equals(g.name) && f.desc.equals("[I")) {
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Transformations", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Transformations");
    }

    private ClassField findTransformationVarbit(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.INVOKESTATIC};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", node.name))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((FieldInsnNode) m.instructions.get(i + 1)).desc.equals("I")) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("TransformationVarbit", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("TransformationVarbit");
    }

    private ClassField findTransformationVarp(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.IALOAD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", node.name))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((FieldInsnNode) m.instructions.get(i + 2)).desc.equals("I")) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("TransformationVarp", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("TransformationVarp");
    }

    private ClassField findGetModelMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(II[[IIII)L%s;", Main.get("Model")))) {
                return new ClassField("*GetModel", m.name, m.desc);
            }
        }
        return new ClassField("*GetModel");
    }
}
