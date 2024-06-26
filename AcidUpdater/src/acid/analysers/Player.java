package acid.analysers;

import acid.Main;
import acid.other.DeprecatedFinder;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.function.Function;

/**
 * Created by Kira on 2014-12-07.
 */
public class Player extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Entity"))) {
                continue;
            }

            for (FieldNode f : n.fields) {
                if (f.desc.equals("Ljava/lang/String;")) {
                    return n;
                }

                if (f.desc.equals("[Ljava/lang/String;")) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Player", node.name);
        info.putField(findName(node));
        info.putField(findModel(node));
        info.putField(findVisible(node));
        info.putField(findDefinition(node));
        info.putField(findCombatLevel(node));
        info.putField(findActions(node));
        Main.getInfo("Entity").setField(findEntityOrientation(node));
        info.putField(findCombatLevel(node));
        info.putField(findIndex(node));
        info.putField(findIsAnimating(node));
        info.putField(findGetModel(node));
        //Main.getInfo("Entity").setField(findEntityAnimationFrames(node));
        Main.getInfo("Entity").setField(findEntitySpotAnimation(node));
        Main.getInfo("Entity").setField(findEntitySpotAnimationFrame(node));
        //Main.getInfo("Model").putField(findModelFitsSingleTile(node));
        info.putField(findOverheadPKIcon(node));
        info.putField(findOverheadPrayerIcon(node));
        return info;
    }

    private ClassField findName(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("Ljava/lang/String;")) {
                return new ClassField("Name", f.name, f.desc);
            }
        }

        //Changed February 1st, 2018..
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("NameInfo")))) {
                return new ClassField("Name", f.name, f.desc);
            }
        }

        return new ClassField("Name");
    }

    private ClassField findModel(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("Model")))) {
                return new ClassField("Model", f.name, f.desc);
            }
        }
        return new ClassField("Model");
    }

    private ClassField findVisible(ClassNode node) {
        final int pattern[] = new int[]{Opcodes.PUTFIELD, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_0, Finder.COMPARISON, Opcodes.GETSTATIC};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;)V", Main.get("Stream")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    if (f.desc.equals("Z")) {
                        return new ClassField("isHidden", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("isHidden");
    }

    private ClassField findDefinition(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("PlayerDefinition")))) {
                return new ClassField("Definition", f.name, f.desc);
            }
        }
        return new ClassField("Definition");
    }

    private ClassField findCombatLevel(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;IIII)V", Main.get("NPCDefinition"))) || m.desc.equals(String.format("(L%s;IIII)V", Main.get("NPC")))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        long multi = (int) ((LdcInsnNode)m.instructions.get(i + 6)).cst;
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 5);
                        return new ClassField("CombatLevel", f.name, f.desc, multi);
                    }
                }
            }
        }
        return new ClassField("CombatLevel");
    }

    private ClassField findActions(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[Ljava/lang/String;")) {
                return new ClassField("Actions", f.name, f.desc);
            }
        }
        return new ClassField("Actions");
    }

    private ClassField findGetModel(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                return new ClassField("*GetModel", m.name, m.desc);
            }
        }
        return new ClassField("*GetModel");
    }

    private ClassField findIndex(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ICONST_0, Opcodes.ICONST_0, Opcodes.ICONST_0, Opcodes.ICONST_0, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;Z)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        long multi = (int) ((LdcInsnNode)m.instructions.get(i + 6)).cst;
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 5);
                        return new ClassField("Index", f.name, f.desc, multi);
                    }
                }
            }
        }
        return new ClassField("Index");
    }

    private ClassField findIsAnimating(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IFNE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    if (f.desc.equals("Z") && f.owner.equals(node.name)) {
                        return new ClassField("IsAnimating", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("IsAnimating");
    }

    private ClassField findOverheadPKIcon(ClassNode node) {
        final int pattern[] = new int[]{
                //                                                      PK Icon
                Opcodes.INVOKEVIRTUAL, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD, Opcodes.ALOAD,
                //                                                                  Prayer Icon
                Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;)V", Main.get("Stream")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                    if (f.desc.equals("I")) {
                        return new ClassField("OverheadSkulledIcon", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("OverheadSkulledIcon");
    }

    private ClassField findOverheadPrayerIcon(ClassNode node) {
        final int pattern[] = new int[]{
                //                                                      PK Icon
                Opcodes.INVOKEVIRTUAL, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD, Opcodes.ALOAD,
                //                                                                  Prayer Icon
                Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;)V", Main.get("Stream")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (m.instructions.get(i + 9) instanceof FieldInsnNode) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 9);
                        if (f.desc.equals("I")) {
                            return new ClassField("OverheadPrayerIcon", f.name, f.desc);
                        }
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("OverheadPrayerIcon");
    }

    private ClassField findIsAutoChatting(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IFNE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.matches(String.format("\\(L%s;III[II]{0,2}\\)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while(i != -1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        if (f.desc.equals("Z") && f.owner.equals(node.name)) {
                            return new ClassField("IsAutoChatting", f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("IsAutoChatting"); //IsAnimating
    }

    private ClassField findEntityOrientation(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Finder.OPTIONAL, Opcodes.IMUL, Opcodes.SIPUSH, Opcodes.IF_ICMPNE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (m.instructions.get(i + 2) instanceof LdcInsnNode) {
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        return new ClassField("Orientation", f.name, f.desc, multi);
                    } else if (m.instructions.get(i - 1) instanceof LdcInsnNode) {
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i - 1)).cst;
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        return new ClassField("Orientation", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Orientation");
    }

    private ClassField findEntityAnimationFrames(ClassNode node) {
        int[] pattern = {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD};
        for (MethodNode m : node.methods) {
            if (hasAccess(m, Opcodes.ACC_PROTECTED) && !hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 2)).var == 2) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 4);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("AnimationFrame", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("AnimationFrame");
    }

    private ClassField findEntitySpotAnimation(ClassNode node) {
        Function<FieldInsnNode, Boolean> hasEntityParent = (FieldInsnNode f) -> {
            return Main.getClassNode("Entity").fields.stream().filter(field -> field.name.equals(f.name)).count() > 0;
        };

        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.INVOKESTATIC, Opcodes.ALOAD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                int i = new DeprecatedFinder(m).findPattern(pattern, 0, false);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    if (f.owner.equals(Main.get("Entity")) || hasEntityParent.apply(f)) {
                        int multi = (int)((LdcInsnNode)m.instructions.get(i + 2)).cst;
                        return new ClassField("SpotAnimation", f.name, f.desc, multi);
                    }
                    i = new DeprecatedFinder(m).findPattern(pattern, i + 1, false);
                }
            }
        }
        return new ClassField("SpotAnimation");
    }

    private ClassField findEntitySpotAnimationFrame(ClassNode node) {
        Function<FieldInsnNode, FieldNode> getEntityParent = (FieldInsnNode f) -> {
            return Main.getClassNode("Entity").fields.stream().filter(field -> field.name.equals(f.name)).findFirst().get();
        };

        final int[] pattern = new int[]{Opcodes.INVOKESTATIC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.INVOKEVIRTUAL};
        final int[] pattern2 = new int[]{Opcodes.ALOAD, Opcodes.ICONST_0, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                int i = new DeprecatedFinder(m).findPattern(pattern, 0, false);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                    if (f.owner.equals(Main.get("Entity"))) {
                        int multi = (int)((LdcInsnNode)m.instructions.get(i + 3)).cst;
                        return new ClassField("SpotAnimationFrame", f.name, f.desc, multi);
                    }

                    FieldNode ff = getEntityParent.apply(f);
                    if (ff != null) {
                        long multi = Main.findMultiplier(Main.get("Entity"), ff.name);
                        return new ClassField("SpotAnimationFrame", ff.name, ff.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1, false);
                }

                i = new DeprecatedFinder(m).findPattern(pattern2, 0, false);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                    if (f.owner.equals(Main.get("Entity"))) {
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("SpotAnimationFrame", f.name, f.desc, multi);
                    }

                    FieldNode ff = getEntityParent.apply(f);
                    if (ff != null) {
                        long multi = Main.findMultiplier(Main.get("Entity"), ff.name);
                        return new ClassField("SpotAnimationFrame", ff.name, ff.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern2, i + 1, false);
                }
            }
        }
        return new ClassField("SpotAnimationFrame");
    }

    private ClassField findModelFitsSingleTile(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("()L%s;", Main.get("Model")))) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.ICONST_1, Opcodes.PUTFIELD});
                FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                if (f.desc.equals("Z")) {
                    return new ClassField("FitsSingleTile", f.name, f.desc);
                }
            }
        }
        return new ClassField("FitsSingleTile");
    }
}
