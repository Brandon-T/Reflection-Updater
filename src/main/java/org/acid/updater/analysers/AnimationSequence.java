package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Brandon on 2014-12-09.
 */
public class AnimationSequence extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("CacheableNode"))) {
                continue;
            }

            int int_arr_count = 0, bool_count = 0, model_method_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("[I")) {
                    ++int_arr_count;
                } else if (f.desc.equals("Z")) {
                    ++bool_count;
                }
            }

            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;II)L%s;", Main.get("Model"), Main.get("Model")))) {
                    ++model_method_count;
                }
            }

            if (int_arr_count >= 1 && bool_count >= 1 && model_method_count > 0) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("AnimationSequence", node);
        info.putField(findAnimationFrames(node));
        info.putField(findAnimationSequenceCache(node));
        info.putField(findAnimationFrameCache(node));
        return info;
    }

    private ClassField findAnimationFrames(ClassNode node) {
        String modelName = Main.get("Model");
        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;I)L%s;", modelName, modelName))) {
                int i = new Finder(m).findPattern(pattern);
                if (((FieldInsnNode) m.instructions.get(i + 1)).desc.equals("[I") && ((VarInsnNode) m.instructions.get(i + 2)).var == 2) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    return new ClassField("Frames", f.name, f.desc);
                }
            }
        }
        return new ClassField("Frames");
    }

    private ClassField findAnimationSequenceCache(ClassNode node) {
        int[] pattern = new int[]{Opcodes.NEW, Opcodes.DUP, Opcodes.BIPUSH, Opcodes.INVOKESPECIAL, Opcodes.PUTSTATIC};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (m.instructions.get(i + 4) instanceof FieldInsnNode f && ((IntInsnNode) m.instructions.get(i + 2)).operand == 64) {
                        return new ClassField("SequenceCache", f.name, f.desc);
                    }

                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("SequenceCache");
    }

    private ClassField findAnimationFrameCache(ClassNode node) {
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.I2L, Opcodes.INVOKEVIRTUAL, Opcodes.CHECKCAST};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(I)L%s;", Main.get("AnimationFrames")))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        return new ClassField("FrameCache", f.name, f.desc);
                    }
                }
            }
        }
        return new ClassField("FrameCache");
    }
}
