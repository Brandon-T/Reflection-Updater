package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-16.
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
        info.putField(findItems(node));
        info.putField(findItemStackSizes(node, info.getField("Items")));
        info.putField(findItemAmount(node));
        info.putField(findSpriteID(node));
        info.putField(findTextureID(node));
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
        info.putField(findSwapItemsMethod(node));
        return info;
    }

    private ClassField findName(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.INVOKEVIRTUAL, Opcodes.LDC};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode)m.instructions.get(i)).var == 9 && ((MethodInsnNode)m.instructions.get(i + 2)).name.contains("append")) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            return new ClassField("Name", f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
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
                    while(i != -1) {
                        if (((VarInsnNode)m.instructions.get(i)).var == 11 && ((VarInsnNode)m.instructions.get(i + 2)).var == 23) {
                            FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
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
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Stream")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if ((int)((LdcInsnNode)m.instructions.get(i + 4)).cst == -65536) {
                        long multi = (int) ((LdcInsnNode)m.instructions.get(i + 2)).cst;
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
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
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Stream")))) {
                int i = new Finder(m).findPattern(pattern, 0, false);
                if (i != -1) {
                    FieldInsnNode f = null;
                    if (m.instructions.get(i + 2) instanceof FieldInsnNode) {
                        f = (FieldInsnNode) m.instructions.get(i + 2);
                    } else {
                        f = (FieldInsnNode) m.instructions.get(i + 3);
                    }
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("ParentID", f.name, f.desc, multi);
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
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL};
        final int[] pattern2 = new int[]{Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.ALOAD, Opcodes.INVOKESTATIC, Opcodes.INVOKESTATIC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 5)).cst;
                        return new ClassField("ItemID", f.name, f.desc, multi);
                    }

                    //April 30th, 2017.
                    i = new Finder(m).findPattern(pattern2);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 6);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 7)).cst;
                        return new ClassField("ItemID", f.name, f.desc, multi);
                    }
                }
            }
        }
        return new ClassField("ItemID");
    }

    private ClassField findItems(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(II)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    if (((VarInsnNode)m.instructions.get(i + 4)).var == 4) {
                        return new ClassField("Items", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Items");
    }

    private ClassField findItemStackSizes(ClassNode node, ClassField items) {
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(II)V")) {
                int i = new Finder(m).findNext(0, Opcodes.GETFIELD);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    if (!f.name.equals(items.getName())) {
                        return new ClassField("ItemStackSizes", f.name, f.desc);
                    }
                    i = new Finder(m).findNext(i + 1, Opcodes.GETFIELD);
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
                            long multi = (int) ((LdcInsnNode)m.instructions.get(i + 3)).cst;
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
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(Z)L%s;", Main.get("ImageRGB")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 4)).var == 3) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                        return new ClassField("SpriteID", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("SpriteID");
    }

    private ClassField findTextureID(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(Z)L%s;", Main.get("ImageRGB")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 4)).var == 3) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (!f.name.equals(findSpriteID(node).getName())) { //TextureID comes after SpriteID
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("TextureID", f.name, f.desc, multi);
                        }
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("TextureID");
    }

    private ClassField findActions(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ICONST_5, Opcodes.ANEWARRAY, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Stream")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
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
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Stream")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    long multi = (int) ((LdcInsnNode)m.instructions.get(i + 2)).cst;
                    return new ClassField("ActionType", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("ActionType");
    }

    private ClassField findType(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_3};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;)V", Main.get("Stream")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    long multi = (int) ((LdcInsnNode)m.instructions.get(i + 2)).cst;
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
                    while(i != -1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
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
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 9);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("AbsoluteX", f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("AbsoluteX");
    }

    private ClassField findAbsoluteY(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 10);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("AbsoluteY", f.name, f.desc, multi);
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
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 15);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("Width", f.name, f.desc, multi);
                }
            }
        }

        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Finder.OPTIONAL, Opcodes.IADD, Opcodes.ISTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("([L%s;IIIIIII)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while(i != -1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        if (m.instructions.get(i + 5) instanceof VarInsnNode && ((VarInsnNode)m.instructions.get(i + 5)).var == 17 || m.instructions.get(i + 6) instanceof VarInsnNode && ((VarInsnNode)m.instructions.get(i + 6)).var == 17) {
                            long multi = (int) ((LdcInsnNode)m.instructions.get(i + 2)).cst;
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
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 16);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("Height", f.name, f.desc, multi);
                }
            }
        }

        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Finder.OPTIONAL, Opcodes.IADD, Opcodes.ISTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("([L%s;IIIIIII)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while(i != -1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        if (m.instructions.get(i + 5) instanceof VarInsnNode && ((VarInsnNode)m.instructions.get(i + 5)).var == 18 || m.instructions.get(i + 6) instanceof VarInsnNode && ((VarInsnNode)m.instructions.get(i + 6)).var == 18) {
                            long multi = (int) ((LdcInsnNode)m.instructions.get(i + 2)).cst;
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
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.AALOAD};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(II)L%s;", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (f.owner.equals(node.name) && ((VarInsnNode) m.instructions.get(i)).var == 3) {
                            return new ClassField("Children", f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
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
                            long multi = (int) ((LdcInsnNode)m.instructions.get(i + 3)).cst;
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

    private ClassField findSwapItemsMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(II)V")) {
                return new ClassField("*SwapItems", m.name, m.desc);
            }
        }
        return new ClassField("*SwapItems");
    }
}
