package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Brandon on 2014-12-14.
 */
public class GameObject extends Analyser {

    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        ClassInfo info = Main.getInfo("Scene");
        if (info != null) {
            String gameObject_class = info.getField("GameObjects").getDesc();
            for (ClassNode n : nodes) {
                if (gameObject_class.equals(String.format("[L%s;", n.name))) {
                    return n;
                }
            }
        }

        ClassNode n = Main.getClassNode("Scene");
        if (n != null) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(IIIIIIIIL%s;IZJI)Z", Main.get("Renderable")))) {
                    for (AbstractInsnNode i : m.instructions.toArray()) {
                        if (i instanceof FieldInsnNode f) {
                            if (f.desc.equals("I") && !f.owner.matches("([ISBJZ])")) {
                                ClassNode nn = Main.getClass(f.owner);
                                if (nn.superName.equals("java/lang/Object")) {
                                    return nn;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        boolean isLongHashes = false;
        ClassNode n = Main.getClassNode("Scene");
        for (MethodNode m : n.methods) {
            if (m.desc.matches(String.format("\\(IIIIIIIIL%s;IZ(I|J)I\\)Z", Main.get("Renderable")))) {
                if (m.desc.contains("JI")) {
                    isLongHashes = true;
                }
                break;
            }
        }

        ClassInfo info = new ClassInfo("GameObject", node);
        info.putField(findField(node, "ID", 12)); //findID(node)
        info.putField(findField(node, "Flags", isLongHashes ? 14 : 13));
        info.putField(findField(node, "Orientation", 10));
        info.putField(findField(node, "Plane", 1));
        info.putField(findField(node, "Height", 8));
        info.putField(findField(node, "X", 6));
        info.putField(findField(node, "Y", 7));
        info.putField(findRelativeX(node));
        info.putField(findRelativeY(node));
        info.putField(findSizeX(node));
        info.putField(findSizeY(node));
        info.putField(findRenderable(node));
        return info;
    }

    private ClassField findRenderable(ClassNode node) {
        ClassNode n = Main.getClassNode("Scene");
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.PUTFIELD};

        for (MethodNode m : n.methods) {
            if (m.desc.matches(String.format("\\(IIIIIIIIL%s;IZ(I|J)I\\)Z", Main.get("Renderable")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i)).var == 9) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Renderable", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Renderable");
    }

    private ClassField findID(ClassNode node) {
        ClassNode n = Main.getClassNode("Scene");
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Finder.WILDCARD, Opcodes.ISHR};

        for (MethodNode m : n.methods) {
            if (m.desc.equals(String.format("(III)L%s;", node.name))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;
                    return new ClassField("ID", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("ID");
    }

    private ClassField findRelativeX(ClassNode node) {
        ClassNode n = Main.getClassNode("Scene");
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD};

        for (MethodNode m : n.methods) {
            if (m.desc.equals(String.format("(III)L%s;", node.name))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 3)).var == 2) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;
                        return new ClassField("RelativeX", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("RelativeX");
    }

    private ClassField findRelativeY(ClassNode node) {
        ClassNode n = Main.getClassNode("Scene");
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD};

        for (MethodNode m : n.methods) {
            if (m.desc.equals(String.format("(III)L%s;", node.name))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 3)).var == 3) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;
                        return new ClassField("RelativeY", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("RelativeY");
    }

    private ClassField findSizeX(ClassNode node) {
        ClassNode n = Main.getClassNode("Scene");
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.IADD, Opcodes.ICONST_1, Opcodes.ISUB, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};

        for (MethodNode m : n.methods) {
            if (m.desc.matches(String.format("\\(IIIIIIIIL%s;IZ(I|J)I\\)Z", Main.get("Renderable")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    VarInsnNode a = (VarInsnNode) m.instructions.get(i);
                    VarInsnNode b = (VarInsnNode) m.instructions.get(i + 1);
                    if ((a.var == 2 && b.var == 4) || (a.var == 4 && b.var == 2)) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 7);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("SizeX", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("SizeX");
    }

    private ClassField findSizeY(ClassNode node) {
        ClassNode n = Main.getClassNode("Scene");
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.IADD, Opcodes.ICONST_1, Opcodes.ISUB, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};

        for (MethodNode m : n.methods) {
            if (m.desc.matches(String.format("\\(IIIIIIIIL%s;IZ(I|J)I\\)Z", Main.get("Renderable")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    VarInsnNode a = (VarInsnNode) m.instructions.get(i);
                    VarInsnNode b = (VarInsnNode) m.instructions.get(i + 1);
                    if ((a.var == 3 && b.var == 5) || (a.var == 5 && b.var == 3)) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 7);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("SizeY", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("SizeY");
    }

    private ClassField findField(ClassNode node, String fieldName, int index) {
        ClassNode n = Main.getClassNode("Scene");
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        final int[] pattern2 = new int[]{Opcodes.LLOAD, Opcodes.LDC, Opcodes.LMUL, Opcodes.PUTFIELD};

        for (MethodNode m : n.methods) {
            if (m.desc.matches(String.format("\\(IIIIIIIIL%s;IZ(I|J)I\\)Z", Main.get("Renderable")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i)).var == index) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField(fieldName, f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }

                //May 10th, 2018 - All hashes changed to Long.
                i = new Finder(m).findPattern(pattern2);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i)).var == index) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField(fieldName, f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern2, i + 1);
                }
            }
        }
        return new ClassField(fieldName);
    }
}
