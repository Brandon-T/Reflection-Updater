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
 * Created by Brandon on 2014-12-16.
 */
public class Widget extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Node"))) {
                continue;
            }

            int obj_arr = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("[Ljava/lang/Object;") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++obj_arr;
                }
            }

            if (obj_arr >= 15) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Widget", node.name);
        info.putField(findName(node));
        info.putField(findText(node));
        info.putField(findID(node));
        info.putField(findParentID(node));
        info.putField(findParent(node));
        info.putField(findItemID(node));
        info.putField(findItemIDs(node));
        info.putField(findItemStackSizes(node, info.getField("Items")));
        info.putField(findItemAmount(node));
        info.putField(findSpriteID(node));
        info.putField(findTextureID(node, info.getField("SpriteID")));
        info.putField(findModelID(node));
        info.putField(findAnimationID(node));
        info.putField(findActions(node));
        info.putField(findActionType(node));
        info.putField(findType(node));
        info.putField(findIsHidden(node));
        info.putField(findAbsoluteX(node));
        info.putField(findAbsoluteY(node));
        info.putField(findRelativeX(node));
        info.putField(findRelativeY(node));
        info.putField(findScrollX(node));
        info.putField(findScrollY(node));
        info.putField(findWidth(node));
        info.putField(findHeight(node));
        info.putField(findChildren(node));
        info.putField(findBoundsIndex(node));
        info.putField(findWidgetCycle(node));
        info.putField(findOpacity(node));
        info.putField(findSwapItemsMethod(node));
        return info;
    }

    private ClassField findName(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.PUTFIELD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.BIPUSH};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Buffer")))) {
                int i = new DeprecatedFinder(m).findPattern(pattern, 0, false);
                while (i != -1) {
                    if (m.instructions.get(i + 3) instanceof FieldInsnNode f) {
                        if (f.desc.equals("Ljava/lang/String;")) {
                            return new ClassField("Name", f.name, f.desc);
                        }
                    }
                    i = new DeprecatedFinder(m).findPattern(pattern, i + 1, false);
                }
            }
        }
        return new ClassField("Name");
    }

    private ClassField findText(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ASTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("([L%s;IIIIIIII)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i)).var == 11 && ((VarInsnNode) m.instructions.get(i + 2)).var == 23) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            return new ClassField("Text", f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("Text");
    }

    private ClassField findID(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.LDC};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Buffer")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if ((int) ((LdcInsnNode) m.instructions.get(i + 4)).cst == -65536) {
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        return new ClassField("ID", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("ID");
    }

    private ClassField findParentID(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.IADD, Finder.OPTIONAL, Opcodes.IMUL, Opcodes.PUTFIELD, Opcodes.ALOAD, Opcodes.ALOAD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Buffer")))) {
                int i = new Finder(m).findPattern(pattern, 0, false);
                if (i != -1) {
                    FieldInsnNode f = null;
                    if (m.instructions.get(i + 2) instanceof FieldInsnNode) {
                        f = (FieldInsnNode) m.instructions.get(i + 2);
                    } else if (m.instructions.get(i + 3) instanceof FieldInsnNode) {
                        f = (FieldInsnNode) m.instructions.get(i + 3);
                    }

                    if (f != null) {
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("ParentID", f.name, f.desc, multi);
                    }
                }
            }
        }
        return new ClassField("ParentID");
    }

    private ClassField findParent(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", node.name))) {
                return new ClassField("Parent", f.name, f.desc);
            }
        }
        return new ClassField("Parent");
    }

    private ClassField findItemID(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL,
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL,
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL,
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL,
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL,
                Opcodes.ICONST_0};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("([L%s;IIIIIIII)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                        return new ClassField("ItemID", f.name, f.desc, multi);
                    }
                }
            }
        }
        return new ClassField("ItemID");
    }

    private ClassField findItemIDs(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ARRAYLENGTH};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;I)I", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        if (f.desc.equals("[I")) {
                            if (((VarInsnNode) m.instructions.get(i)).var == 13) {
                                return new ClassField("ItemIDs", f.name, f.desc);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("ItemIDs");
    }

    private ClassField findItemStackSizes(ClassNode node, ClassField items) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;I)I", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        if (f.desc.equals("[I")) {
                            if (((VarInsnNode) m.instructions.get(i)).var == 8) {
                                return new ClassField("ItemStackSizes", f.name, f.desc);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("ItemStackSizes");
    }

    private ClassField findItemAmount(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.INVOKEVIRTUAL};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("([L%s;IIIIIIII)V", node.name)) && hasAccess(m, Opcodes.ACC_STATIC)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i)).var == 24 && ((VarInsnNode) m.instructions.get(i + 1)).var == 11) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 3)).cst;
                            return new ClassField("ItemAmount", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("ItemAmount");
    }

    private ClassField findSpriteID(ClassNode node) {
        int[] pattern = new int[]{
                Opcodes.ICONST_5,
                Finder.COMPARISON,

                // SpriteID
                Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD,

                // TextureID
                Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD
        };

        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Buffer")))) {
                List<AbstractInsnNode> insns = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                while (insns != null) {
                    MethodInsnNode a = (MethodInsnNode) insns.get(4);
                    MethodInsnNode b = (MethodInsnNode) insns.get(10);

                    if (a.name.equals(b.name) && a.desc.equals(b.desc)) {
                        FieldInsnNode f = (FieldInsnNode) insns.get(7);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("SpriteID", f.name, f.desc, multi);
                    }

                    insns = new DeprecatedFinder(m).findPatternInstructions(pattern, m.instructions.indexOf(insns.getFirst()) + 1, false);
                }
            }
        }
        return new ClassField("SpriteID");
    }

    private ClassField findTextureID(ClassNode node, ClassField spriteID) {
        int[] pattern = new int[]{
                Opcodes.ICONST_5,
                Finder.COMPARISON,

                // SpriteID
                Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD,

                // TextureID
                Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD
        };

        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Buffer")))) {
                List<AbstractInsnNode> insns = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                while (insns != null) {
                    MethodInsnNode a = (MethodInsnNode) insns.get(4);
                    MethodInsnNode b = (MethodInsnNode) insns.get(10);

                    if (a.name.equals(b.name) && a.desc.equals(b.desc)) {
                        FieldInsnNode f = (FieldInsnNode) insns.get(13);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("TextureID", f.name, f.desc, multi);
                    }

                    insns = new DeprecatedFinder(m).findPatternInstructions(pattern, m.instructions.indexOf(insns.getFirst()) + 1, false);
                }
            }
        }
        return new ClassField("TextureID");
    }

    private ClassField findModelID(ClassNode node) {
        int[] pattern = new int[]{
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.LDC,
                Finder.COMPARISON,
                Opcodes.ALOAD, Opcodes.LDC, Opcodes.PUTFIELD
        };

        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Buffer")))) {
                List<AbstractInsnNode> insns = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                while (insns != null) {
                    if ((int) ((LdcInsnNode) insns.get(4)).cst == 65535) {
                        FieldInsnNode f = (FieldInsnNode) insns.get(1);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("ModelID", f.name, f.desc, multi);
                    }
                    insns = new DeprecatedFinder(m).findPatternInstructions(pattern, m.instructions.indexOf(insns.getFirst()) + 1, false);
                }
            }
        }
        return new ClassField("ModelID");
    }

    private ClassField findAnimationID(ClassNode node) {
        int[] pattern = new int[]{
                Opcodes.ASTORE, Opcodes.ILOAD,
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL
        };

        for (MethodNode m : Main.getClass("client").methods) {
            if (m.desc.equals(String.format("(L%s;)Z", Main.get("PacketWriter")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("AnimationID", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("AnimationID");
    }

    private ClassField findActions(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IFNULL};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(ILjava/lang/String;)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    if (f.desc.equals("[Ljava/lang/String;")) {
                        return new ClassField("Actions", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Actions");
    }

    private ClassField findActionType(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_5, Opcodes.IF_ICMPEQ};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Buffer")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                    return new ClassField("ActionType", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("ActionType");
    }

    private ClassField findType(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_3};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Buffer")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                    return new ClassField("Type", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("Type");
    }

    private ClassField findIsHidden(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 20);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    return new ClassField("IsHidden", f.name, f.desc);
                }
            }
        }

        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IRETURN};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;)Z", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (f.owner.equals(node.name)) {
                            return new ClassField("isHidden", f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("IsHidden");
    }

    private ClassField findAbsoluteX(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();

        final int[] prefixPattern = new int[]{Opcodes.ALOAD, Opcodes.ICONST_0, Opcodes.FALOAD};
        final int[] pattern = new int[]{Opcodes.FMUL, Opcodes.F2I, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};

        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(IL%s;IIIII[F)L%s;", node.name, node.name))) {
                    int i = new Finder(m).findPattern(prefixPattern);
                    while (i != -1) {
                        int j = new Finder(m).findPattern(pattern, i);
                        if (j != -1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(j + 4);
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("AbsoluteX", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(prefixPattern, i + 1);
                    }
                }
            }
        }

        return new ClassField("AbsoluteX");
    }

    private ClassField findAbsoluteY(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();

        final int[] prefixPattern = new int[]{Opcodes.ALOAD, Opcodes.ICONST_1, Opcodes.FALOAD};
        final int[] pattern = new int[]{Opcodes.FMUL, Opcodes.F2I, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};

        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(IL%s;IIIII[F)L%s;", node.name, node.name))) {
                    int i = new Finder(m).findPattern(prefixPattern);
                    while (i != -1) {
                        int j = new Finder(m).findPattern(pattern, i);
                        if (j != -1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(j + 4);
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("AbsoluteY", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(prefixPattern, i + 1);
                    }
                }
            }
        }

        return new ClassField("AbsoluteY");
    }

    private ClassField findRelativeX(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 13);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("RelativeX", f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("RelativeX");
    }

    private ClassField findRelativeY(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 14);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("RelativeY", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("RelativeY");
    }

    private ClassField findScrollX(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 21);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("ScrollX", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("ScrollX");
    }

    private ClassField findScrollY(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 22);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("ScrollY", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("ScrollY");
    }

    private ClassField findWidth(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Finder.OPTIONAL, Opcodes.IADD, Opcodes.ISTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("([L%s;IIIIIII)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (m.instructions.get(i + 5) instanceof VarInsnNode && ((VarInsnNode) m.instructions.get(i + 5)).var == 17 || m.instructions.get(i + 6) instanceof VarInsnNode && ((VarInsnNode) m.instructions.get(i + 6)).var == 17) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("Width", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("Width");
    }

    private ClassField findHeight(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Finder.OPTIONAL, Opcodes.IADD, Opcodes.ISTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("([L%s;IIIIIII)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (m.instructions.get(i + 5) instanceof VarInsnNode && ((VarInsnNode) m.instructions.get(i + 5)).var == 18 || m.instructions.get(i + 6) instanceof VarInsnNode && ((VarInsnNode) m.instructions.get(i + 6)).var == 18) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("Height", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("Height");
    }

    private ClassField findChildren(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[L%s;", node.name))) {
                return new ClassField("Children", f.name, f.desc);
            }
        }
        return new ClassField("Children");
    }

    private ClassField findBoundsIndex(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_1, Opcodes.BASTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("([L%s;IIIIIIII)V", node.name)) && hasAccess(m, Opcodes.ACC_STATIC)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 1)).var == 11) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 3)).cst;
                            return new ClassField("BoundsIndex", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("BoundsIndex");
    }

    private ClassField findWidgetCycle(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("([L%s;IIIIIIII)V", node.name)) && hasAccess(m, Opcodes.ACC_STATIC)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i)).var == 11) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("WidgetCycle", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("WidgetCycle");
    }

    private ClassField findOpacity(ClassNode node) {

        return new ClassField("Opacity");
    }

    private ClassField findSwapItemsMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(II)V")) {
                return new ClassField("*SwapItems", m.name, m.desc);
            }
        }
        return new ClassField("*SwapItems");
    }
}
