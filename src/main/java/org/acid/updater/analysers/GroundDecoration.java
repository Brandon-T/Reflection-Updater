package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-14.
 */
public class GroundDecoration extends Analyser {
    private MethodNode method = null;

    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        ClassNode n = Main.getClassNode("Region");
        if (n != null) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(IIIIL%s;JI)V", Main.get("Animable")))) {
                    for (AbstractInsnNode i : m.instructions.toArray()) {
                        if (i instanceof FieldInsnNode) {
                            FieldInsnNode f = (FieldInsnNode) i;
                            if (f.desc.equals("I") && !f.owner.matches("(I|S|B|J|Z)")) {
                                this.method = m;
                                return Main.getClass(f.owner);
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
        ClassInfo info = new ClassInfo("GroundDecoration", node.name);
        info.putField(findField(node, "ID", 6));
        info.putField(findField(node, "Flags", this.method.desc.contains("JI") ? 8 : 7));
        info.putField(findX(node));
        info.putField(findY(node));
        info.putField(findField(node, "Plane", 4));
        info.putField(findRenderable(node));
        return info;
    }

    private ClassField findX(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.LDC, Opcodes.IADD, Opcodes.PUTFIELD};
        if (this.method != null) {
            int i = new Finder(this.method).findPattern(pattern);
            while(i != -1) {
                if (((VarInsnNode)method.instructions.get(i)).var == 2) {
                    FieldInsnNode f = (FieldInsnNode)method.instructions.get(i + 5);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("X", f.name, f.desc, multi);
                }
                i = new Finder(this.method).findPattern(pattern, i + 1);
            }
        }
        return new ClassField("X");
    }

    private ClassField findY(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.LDC, Opcodes.IADD, Opcodes.PUTFIELD};
        if (this.method != null) {
            int i = new Finder(this.method).findPattern(pattern);
            while(i != -1) {
                if (((VarInsnNode)method.instructions.get(i)).var == 3) {
                    FieldInsnNode f = (FieldInsnNode)method.instructions.get(i + 5);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("Y", f.name, f.desc, multi);
                }
                i = new Finder(this.method).findPattern(pattern, i + 1);
            }
        }
        return new ClassField("Y");
    }

    private ClassField findRenderable(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.PUTFIELD};
        if (this.method != null) {
            int i = new Finder(this.method).findPattern(pattern);
            while (i != -1) {
                if (((VarInsnNode) method.instructions.get(i)).var == 5) {
                    FieldInsnNode f = (FieldInsnNode) method.instructions.get(i + 1);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("Renderable", f.name, f.desc, multi);
                }
                i = new Finder(this.method).findPattern(pattern, i + 1);
            }
        }
        return new ClassField("Renderable");
    }

    private ClassField findField(ClassNode node, String fieldName, int index) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        if (this.method != null) {
            int i = new Finder(this.method).findPattern(pattern);
            while (i != -1) {
                if (((VarInsnNode)method.instructions.get(i)).var == index) {
                    FieldInsnNode f = (FieldInsnNode) method.instructions.get(i + 3);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField(fieldName, f.name, f.desc, multi);
                }
                i = new Finder(this.method).findPattern(pattern, i + 1);
            }
        }

        final int[] pattern2 = new int[]{Opcodes.LLOAD, Opcodes.LDC, Opcodes.LMUL, Opcodes.PUTFIELD};
        if (this.method != null) {
            int i = new Finder(this.method).findPattern(pattern2);
            while (i != -1) {
                if (((VarInsnNode)method.instructions.get(i)).var == index) {
                    FieldInsnNode f = (FieldInsnNode) method.instructions.get(i + 3);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField(fieldName, f.name, f.desc, multi);
                }
                i = new Finder(this.method).findPattern(pattern2, i + 1);
            }
        }
        return new ClassField(fieldName);
    }
}