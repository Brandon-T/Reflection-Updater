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
public class Scene extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Renderable"))) {
                continue;
            }

            int int_array_fields = 0;
            int int_fields = 0;

            for (FieldNode f : n.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[[[I")) {
                    ++int_array_fields;
                }

                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("I")) {
                    ++int_fields;
                }
            }

            if (int_array_fields == 2 && int_fields >= 6) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Scene", node.name);
        info.putField(findTiles(node));
        info.putField(findGameObjects(node));
        return info;
    }

    private ClassField findTiles(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.MULTIANEWARRAY, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 5);
                    if (f.desc.startsWith("[[[")) {
                        return new ClassField("Tiles", f.name, f.desc);
                    }
                }
            }
        }
        return new ClassField("Tiles");
    }

    private ClassField findGameObjects(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.BIPUSH, Opcodes.ANEWARRAY, Opcodes.PUTSTATIC};
        final int[] pattern2 = new int[]{Opcodes.ALOAD, Opcodes.SIPUSH, Opcodes.ANEWARRAY, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((IntInsnNode) m.instructions.get(i)).operand == 100) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        return new ClassField("GameObjects", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }

            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern2);
                while (i != -1) {
                    if (((IntInsnNode) m.instructions.get(i + 1)).operand == 5000) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                        return new ClassField("GameObjects", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern2, i + 1);
                }
            }
        }
        return new ClassField("GameObjects");
    }
}
