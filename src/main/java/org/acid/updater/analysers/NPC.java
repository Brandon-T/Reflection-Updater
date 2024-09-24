package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Brandon on 2014-12-08.
 */
public class NPC extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Actor"))) {
                continue;
            }

            for (FieldNode f : n.fields) {
                if (f.desc.equals(String.format("L%s;", Main.get("NPCDefinition")))) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("NPC", node);
        info.putField(findDefinition(node));
        Main.getInfo("Actor").putField(findEntityHeight(node));
        Main.getInfo("Actor").setField(findEntityAnimationID(node));
        Main.getInfo("Actor").setField(findEntityAnimationDelay(node));
        Main.getInfo("Actor").setField(findEntityQueueTraversed(node));
        Main.getInfo("Actor").setField(findEntityQueueSize(node));
        Main.getInfo("Actor").setField(findEntityAnimationFrame(node));
        Main.getInfo("Actor").setField(findEntityMovementSequence(node));
        Main.getInfo("Actor").setField(findEntityMovementFrame(node));
        info.putField(findGetModel(node));
        Main.getInfo("NPCDefinition").setField(findModelTileSize(node));
        Main.getInfo("NPCDefinition").setField(findCombatLevel(node));
        return info;
    }

    private ClassField findDefinition(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("NPCDefinition")))) {
                return new ClassField("Definition", f.name, f.desc);
            }
        }
        return new ClassField("Definition");
    }

    private ClassField findGetModel(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                return new ClassField("*GetModel", m.name, m.desc);
            }
        }
        return new ClassField("*GetModel");
    }

    private ClassField findEntityHeight(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 5);
                    long multi = Main.findMultiplier(node.superName, f.name);
                    return new ClassField("Height", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("Height");
    }

    private ClassField findEntityAnimationID(ClassNode node) {
        if (Main.getInfo("Actor").getField("AnimationID").getName().equals("N/A")) {
            for (MethodNode m : node.methods) {
                if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL});
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                        return new ClassField("AnimationID", f.name, f.desc, multi);
                    }
                }
            }

            // Search getModel()
            for (MethodNode m : node.methods) {
                if (m.desc.equals(String.format("()L%s;", Main.get("Model"))) && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_M1});
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                        return new ClassField("AnimationID", f.name, f.desc, multi);
                    }
                }
            }

            return new ClassField("AnimationID");
        }

        return Main.getInfo("Actor").getField("AnimationID");
    }

    private ClassField findEntityAnimationDelay(ClassNode node) {
        if (Main.getInfo("Actor").getField("AnimationDelay").getName().equals("N/A")) {
            // Search getModel()
            for (MethodNode m : node.methods) {
                if (m.desc.equals(String.format("()L%s;", Main.get("Model"))) && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_0});
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                        return new ClassField("AnimationDelay", f.name, f.desc, multi);
                    }
                }
            }
        }
        return Main.getInfo("Actor").getField("AnimationDelay");
    }

    private ClassField findEntityQueueTraversed(ClassNode node) {
        if (Main.getInfo("Actor").getField("QueueTraversed").getName().equals("N/A")) {
            final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ICONST_0, Opcodes.ICONST_1};
            for (MethodNode m : node.methods) {
                if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        return new ClassField("QueueTraversed", f.name, f.desc);
                    }
                }
            }
            return new ClassField("QueueTraversed");
        }

        return Main.getInfo("Actor").getField("QueueTraversed");
    }

    private ClassField findEntityQueueSize(ClassNode node) {
        if (Main.getInfo("Actor").getField("QueueSize").getName().equals("N/A")) {
            final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.BIPUSH};
            for (MethodNode m : node.methods) {
                if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (((IntInsnNode) m.instructions.get(i + 4)).operand == 0x9) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("QueueSize", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
            return new ClassField("QueueSize");
        }

        return Main.getInfo("Actor").getField("QueueSize");
    }

    private ClassField findEntityAnimationFrame(ClassNode node) {
        if (Main.getInfo("Actor").getField("AnimationFrame").getName().equals("N/A")) {
            final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD};
            for (MethodNode m : node.methods) {
                if (m.desc.equals(String.format("()L%s;", Main.get("Model"))) && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (m.instructions.get(i + 2) instanceof FieldInsnNode f) {
                            if (((VarInsnNode) m.instructions.get(i)).var == 2) {
                                long multi = (int) ((LdcInsnNode) m.instructions.get(i + 3)).cst;
                                return new ClassField("AnimationFrame", f.name, f.desc, multi);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
            return new ClassField("AnimationFrame");
        }

        return Main.getInfo("Actor").getField("AnimationFrame");
    }

    private ClassField findEntityMovementSequence(ClassNode node) {
        if (Main.getInfo("Actor").getField("MovementSequence").getName().equals("N/A")) {
            final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_M1};
            for (MethodNode m : node.methods) {
                if (m.desc.equals(String.format("()L%s;", Main.get("Model"))) && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (!f.name.equals(Main.getInfo("Actor").getField("AnimationID").getName())) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("MovementSequence", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
            return new ClassField("MovementSequence");
        }

        return Main.getInfo("Actor").getField("MovementSequence");
    }

    private ClassField findEntityMovementFrame(ClassNode node) {
        if (Main.getInfo("Actor").getField("MovementFrame").getName().equals("N/A")) {
            final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD};
            for (MethodNode m : node.methods) {
                if (m.desc.equals(String.format("()L%s;", Main.get("Model"))) && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (m.instructions.get(i + 2) instanceof FieldInsnNode f) {
                            if (((VarInsnNode) m.instructions.get(i)).var == 3) {
                                long multi = Main.findMultiplier(f.owner, f.name);
                                return new ClassField("MovementFrame", f.name, f.desc, multi);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
            return new ClassField("MovementFrame");
        }

        return Main.getInfo("Actor").getField("MovementFrame");
    }

    private ClassField findModelTileSize(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_1};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                    int multi = (int) ((LdcInsnNode) m.instructions.get(i + 3)).cst;
                    return new ClassField("ModelTileSize", f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("ModelTileSize");
    }

    private ClassField findCombatLevel(ClassNode node) {
        if (Main.getInfo("NPCDefinition").getField("CombatLevel").getName().equals("N/A")) {
            Collection<ClassNode> nodes = Main.getClasses();
            final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL};
            for (ClassNode n : nodes) {
                for (MethodNode m : n.methods) {
                    if (m.desc.equals(String.format("(L%s;IIII)V", node.name))) {
                        int i = new Finder(m).findPattern(pattern);
                        while (i != -1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            if (f.owner.equals(Main.get("NPCDefinition")) && f.desc.equals("I")) {
                                long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                                return new ClassField("CombatLevel", f.name, f.desc, multi);
                            }
                            i = new Finder(m).findPattern(pattern, i + 1);
                        }
                    }
                }
            }
        }
        return Main.getInfo("NPCDefinition").getField("CombatLevel");
    }
}
