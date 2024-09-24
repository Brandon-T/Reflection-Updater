package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.DeprecatedFinder;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.List;

/**
 * Created by Brandon on 2014-12-08.
 */
public class NPCDefinition extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("CacheableNode"))) {
                continue;
            }

            int short_arr_count = 0, str_count = 0, str_arr_count = 0, model_method_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("[S") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++short_arr_count;
                } else if (f.desc.equals("Ljava/lang/String;") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++str_count;
                } else if (f.desc.equals("[Ljava/lang/String;") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++str_arr_count;
                }
            }

            if (str_count == 1 && str_arr_count == 1 && short_arr_count >= 4) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("NPCDefinition", node);
        info.putField(findID(node));
        info.putField(findName(node));
        info.putField(findActions(node));
        info.putField(findModelIDs(node));
        info.putField(findCombatLevel(node));
        info.putField(findVisible(node));
        info.putField(findModelCache(node));
        info.putField(findTransformations(node));
        info.putField(findModelTileSize(node));
        info.putField(findModelScaleWidth(node));
        info.putField(findModelScaleHeight(node));
        info.putField(findTransformVarbit(node));
        info.putField(findTransformVarp(node));
        info.putField(findDefinitionCache(node));
        return info;
    }

    private ClassField findName(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("Ljava/lang/String;")) {
                return new ClassField("Name", f.name, f.desc);
            }
        }
        return new ClassField("Name");
    }

    private ClassField findActions(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[Ljava/lang/String;")) {
                return new ClassField("Actions", f.name, f.desc);
            }
        }
        return new ClassField("Actions");
    }

    private ClassField findID(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.matches(String.format("\\(L%s;IL%s;IL[A-z]{0,2};\\)L%s;", Main.get("AnimationSequence"), Main.get("AnimationSequence"), Main.get("Model")))) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.GETFIELD, Finder.OPTIONAL, Opcodes.IMUL, Opcodes.I2L});
                if (i != -1) {
                    long multi = 0;
                    if (m.instructions.get(i + 1) instanceof LdcInsnNode) {
                        multi = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;
                    } else {
                        multi = (int) ((LdcInsnNode) m.instructions.get(new Finder(m).findPrev(i, Opcodes.LDC))).cst;
                    }
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    return new ClassField("ID", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("ID");
    }

    private ClassField findModelIDs(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.IASTORE};
        for (MethodNode m : node.methods) {
            int i = new Finder(m).findPattern(pattern);
            while (i != -1) {
                if (((VarInsnNode) m.instructions.get(i + 2)).var == 5) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    return new ClassField("ModelIDs", f.name, f.desc);
                }
                i = new Finder(m).findPattern(pattern, i + 1);
            }

        }
        return new ClassField("ModelIDs");
    }

    private ClassField findCombatLevel(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;III)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (f.owner.equals(node.name) && f.desc.equals("I")) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("CombatLevel", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("CombatLevel");
    }

    private ClassField findVisible(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Finder.CONSTANT, Finder.COMPARISON, Opcodes.ALOAD, Opcodes.ICONST_1, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;I)V", Main.get("Buffer")))) {
                int i = new Finder(m).findPattern(pattern, 0, false);
                while (i != -1) {
                    if (m.instructions.get(i + 1) instanceof IntInsnNode && ((IntInsnNode) m.instructions.get(i + 1)).operand == 99) {
                        int j = new Finder(m).findNext(i, Opcodes.PUTFIELD, false);
                        if (j != -1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                            return new ClassField("Visible", f.name, f.desc);
                        }
                    }
                    i = new Finder(m).findPattern(pattern, i + 1, false);
                }
            }
        }
        return new ClassField("Visible");
    }

    private ClassField findModelCache(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.NEW, Opcodes.DUP, Opcodes.BIPUSH, Opcodes.INVOKESPECIAL, Opcodes.PUTSTATIC};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((IntInsnNode) m.instructions.get(i + 2)).operand == 50) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                        return new ClassField("ModelCache", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("ModelCache");
    }

    private ClassField findTransformations(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.LDC};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;I)V", Main.get("Buffer")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1 && ((VarInsnNode) m.instructions.get(i + 2)).var == 6 && (int) ((LdcInsnNode) m.instructions.get(i + 4)).cst == 65535) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    return new ClassField("Transformations", f.name, f.desc);
                }
            }
        }

        return new ClassField("Transformations");
    }

    private ClassField findModelTileSize(ClassNode node) {
        return new ClassField("ModelTileSize");
    }

    private ClassField findModelScaleWidth(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC};
        for (MethodNode m : node.methods) {
            if (m.desc.matches(String.format("\\(L%s;IL%s;IL[A-z]{0,2};\\)L%s;", Main.get("AnimationSequence"), Main.get("AnimationSequence"), Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                    int multi = (int) ((LdcInsnNode) m.instructions.get(i + 3)).cst;
                    return new ClassField("ModelScaleWidth", f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("ModelScaleWidth");
    }

    private ClassField findModelScaleHeight(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC};
        for (MethodNode m : node.methods) {
            if (m.desc.matches(String.format("\\(L%s;IL%s;IL[A-z]{0,2};\\)L%s;", Main.get("AnimationSequence"), Main.get("AnimationSequence"), Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 6);
                    int multi = (int) ((LdcInsnNode) m.instructions.get(i + 7)).cst;
                    return new ClassField("ModelScaleHeight", f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("ModelScaleHeight");
    }

    private ClassField findTransformVarbit(ClassNode node) {
        final int[] pattern = new int[]{
                Opcodes.ICONST_M1, Opcodes.ISTORE,
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_M1
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("()L%s;", node.name))) {
                List<AbstractInsnNode> insns = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                if (insns != null) {
                    FieldInsnNode f = (FieldInsnNode) insns.get(3);
                    int multi = (int) ((LdcInsnNode) insns.get(4)).cst;
                    return new ClassField("TransformVarbit", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("TransformVarbit");
    }

    private ClassField findTransformVarp(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.IALOAD, Opcodes.ISTORE};

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("()L%s;", node.name))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                    int multi = (int) ((LdcInsnNode) m.instructions.get(i + 3)).cst;
                    return new ClassField("TransformVarp", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("TransformVarp");
    }

    private ClassField findDefinitionCache(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.NEW, Opcodes.DUP, Opcodes.BIPUSH, Opcodes.INVOKESPECIAL, Opcodes.PUTSTATIC};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((IntInsnNode) m.instructions.get(i + 2)).operand == 64) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                        return new ClassField("DefinitionCache", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("DefinitionCache");
    }
}
