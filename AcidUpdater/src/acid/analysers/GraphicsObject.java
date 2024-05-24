package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.Collection;

public class GraphicsObject extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Animable"))) {
                continue;
            }

            for (MethodNode m : n.methods) {
                if (m.name.equals("<init>") && m.desc.equals("(IIIIIII)V")) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("GraphicsObject", node.name);
        info.putField(findField(node, "ID", 1));
        info.putField(findField(node, "LocalX", 3));
        info.putField(findField(node, "LocalY", 4));
        info.putField(findField(node, "Height", 5));
        info.putField(findField(node, "Plane", 2));
        info.putField(findSequenceDefinition(node));
        info.putField(findFrame(node));
        info.putField(findFrameCycle(node));
        info.putField(findStartCycle(node));
        info.putField(findIsFinished(node));
        return info;
    }

    ClassField findField(ClassNode node, String name, int index) {
        final int pattern[] = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 1)).var == index) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField(name, f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField(name);
    }

    ClassField findSequenceDefinition(ClassNode node) {
        final int pattern[] = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.INVOKESTATIC, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 3);
                    return new ClassField("SequenceDefinition", f.name, f.desc);
                }
            }
        }
        return new ClassField("SequenceDefinition");
    }

    ClassField findFrame(ClassNode node) {
        String frameCycleName = "";
        final int pattern[] = new int[]{Opcodes.ALOAD, Opcodes.ICONST_0, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                    if (f.desc.equals("I") && !f.name.equals(frameCycleName)) {
                        if (frameCycleName.isEmpty()) {
                            frameCycleName = f.name;
                            continue;
                        }

                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Frame", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Frame");
    }

    ClassField findFrameCycle(ClassNode node) {
        final int pattern[] = new int[]{Opcodes.ALOAD, Opcodes.ICONST_0, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                    if (f.desc.equals("I")) {
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("FrameCycle", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("FrameCycle");
    }

    ClassField findStartCycle(ClassNode node) {
        final int pattern[] = new int[]{Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.IADD, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 4);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("StartCycle", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("StartCycle");
    }

    ClassField findIsFinished(ClassNode node) {
        final int pattern[] = new int[]{Opcodes.ALOAD, Opcodes.ICONST_1, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                    if (f.desc.equals("Z")) {
                        return new ClassField("IsFinished", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("IsFinished");
    }
}