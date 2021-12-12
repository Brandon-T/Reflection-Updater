package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-21.
 */
public class AnimableNode extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Animable"))) {
                continue;
            }

            for (MethodNode m : n.methods) {
                if (m.name.equals("<init>") && m.desc.equals(String.format("(IIIIIIIZL%s;)V", Main.get("Animable")))) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("AnimableNode", node.name);
        info.putField(findField(node, "ID", 1));
        info.putField(findAnimation(node));
        info.putField(findField(node, "Flags", 2));
        info.putField(findField(node, "Orientation", 3));
        info.putField(findField(node, "Plane", 4));
        info.putField(findField(node, "X", 5));
        info.putField(findField(node, "Y", 6));
        info.putField(findAnimationFrame(node));
        return info;
    }

    private ClassField findAnimation(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.ARRAYLENGTH, Opcodes.I2D};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals(String.format("(IIIIIIIZL%s;)V", Main.get("Animable")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    return new ClassField("AnimationSequence", f.name, f.desc);
                }
            }
        }
        return new ClassField("AnimationSequence");
    }

    private ClassField findField(ClassNode node, String fieldName, int index) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals(String.format("(IIIIIIIZL%s;)V", Main.get("Animable")))) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 1)).var == index) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField(fieldName, f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField(fieldName);
    }

    private ClassField findAnimationFrame(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ICONST_0, Opcodes.PUTFIELD};
        final int[] pattern2 = new int[]{Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.IALOAD};

        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals(String.format("(IIIIIIIZL%s;)V", Main.get("Animable")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                    int multi = (int)Main.findMultiplier(f.owner, f.desc);
                    return new ClassField("AnimationFrame", f.name, f.desc, multi);
                }

                int j = new Finder(m).findPattern(pattern2);
                if (j != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(j + 3);
                    int multi = (int) ((LdcInsnNode)m.instructions.get(j + 4)).cst;
                    return new ClassField("AnimationFrame", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("AnimationFrame");
    }
}
