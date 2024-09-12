package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Brandon on 2014-12-07.
 */
public class IndexedImage extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Rasteriser"))) {
                continue;
            }

            for (MethodNode m : n.methods) {
                if (m.desc.equals("([I[B[IIIIIII)V")) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("IndexedRGB", node.name);
        info.putField(findPixels(node));
        info.putField(findPalette(node));
        info.putField(findWidth(node));
        info.putField(findHeight(node));
        return info;
    }

    private ClassField findPixels(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[B")) {
                return new ClassField("Pixels", f.name, f.desc);
            }
        }
        return new ClassField("Pixels");
    }

    private ClassField findPalette(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[I")) {
                return new ClassField("Palette", f.name, f.desc);
            }
        }
        return new ClassField("Palette");
    }

    private ClassField findWidth(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(IIII)V") && !hasAccess(m, Opcodes.ACC_STATIC)) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 2)).var == 9) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = (int) Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Width", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Width");
    }

    private ClassField findHeight(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(IIII)V") && !hasAccess(m, Opcodes.ACC_STATIC)) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 2)).var == 10) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = (int) Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Height", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Height");
    }
}
