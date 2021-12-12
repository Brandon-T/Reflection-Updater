package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-08.
 */
public class NPC extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Entity"))) {
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
        ClassInfo info = new ClassInfo("NPC", node.name);
        info.putField(findDefinition(node));
        Main.getInfo("Entity").putField(findEntityHeight(node));
        Main.getInfo("Entity").setField(findEntityAnimationID(node));
        Main.getInfo("Entity").setField(findEntityQueueTraversed(node));
        Main.getInfo("Entity").setField(findEntityQueueLength(node));
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
        if (Main.getInfo("Entity").getField("AnimationID").getName().equals("N/A")) {
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

            return new ClassField("AnimationID");
        }

        return Main.getInfo("Entity").getField("AnimationID");
    }

    private ClassField findEntityQueueTraversed(ClassNode node) {
        if (Main.getInfo("Entity").getField("QueueTraversed").getName().equals("N/A")) {
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

        return Main.getInfo("Entity").getField("QueueTraversed");
    }

    private ClassField findEntityQueueLength(ClassNode node) {
        if (Main.getInfo("Entity").getField("QueueLength").getName().equals("N/A")) {
            final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.BIPUSH};
            for (MethodNode m : node.methods) {
                if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (((IntInsnNode) m.instructions.get(i + 4)).operand == 0x9) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("QueueLength", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
            return new ClassField("QueueLength");
        }

        return Main.getInfo("Entity").getField("QueueLength");
    }

    private ClassField findModelTileSize(ClassNode node) {
        final int pattern[] = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_1};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                    int multi = (int)((LdcInsnNode)m.instructions.get(i + 3)).cst;
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
                    if (m.desc.equals(String.format("(L%s;III)V", node.name))) {
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
