package acid.analysers;

import acid.Main;
import acid.other.DeprecatedFinder;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Projectile extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Animable"))) {
                continue;
            }

            for (MethodNode m : n.methods) {
                if (m.name.equals("<init>") && m.desc.equals("(IIIIIIIIIII)V")) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Projectile", node.name);
        info.putField(findField(node, "ID", 1));
        info.putField(findField(node, "Plane", 2));
        info.putField(findField(node, "SourceX", 3));
        info.putField(findField(node, "SourceY", 4));
        info.putField(findField(node, "SourceZ", 5));
        info.putField(findX(node));
        info.putField(findY(node));
        info.putField(findZ(node));
        info.putField(findSpeed(node));
        info.putField(findSpeedX(node));
        info.putField(findSpeedY(node));
        info.putField(findSpeedZ(node));
        info.putField(findAccelerationZ(node));
        info.putField(findField(node, "StartHeight", 9));
        info.putField(findField(node, "EndHeight", 11));
        info.putField(findField(node, "StartCycle", 6));
        info.putField(findField(node, "EndCycle", 7));
        info.putField(findField(node, "Slope", 8));
        info.putField(findField(node, "InteractingIndex", 10));
        info.putField(findPitch(node));
        info.putField(findYaw(node));
        info.putField(findIsMoving(node));
        info.putField(findSequenceDefinition(node));
        info.putField(findFrame(node));
        info.putField(findFrameCycle(node));
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

    ClassField findX(ClassNode node) {
        final int pattern[] = new int[]{
                //                     X                                                       SpeedX
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.DLOAD, Opcodes.DDIV, Opcodes.PUTFIELD,
                // Nonsense
                Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.I2D,
                //                     Y                                                       SpeedY
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.DLOAD, Opcodes.DDIV, Opcodes.PUTFIELD,
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIII)V")) {
                int i = new Finder(m).findPattern(pattern, 0, false);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    return new ClassField("X", f.name, f.desc);
                }
            }
        }

        return new ClassField("X");
    }

    ClassField findY(ClassNode node) {
        final int pattern[] = new int[]{
                //                     X                                                       SpeedX
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.DLOAD, Opcodes.DDIV, Opcodes.PUTFIELD,
                // Nonsense
                Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.I2D,
                //                     Y                                                       SpeedY
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.DLOAD, Opcodes.DDIV, Opcodes.PUTFIELD,
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIII)V")) {
                List<AbstractInsnNode> nodes = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                if (nodes != null) {
                    FieldInsnNode f = (FieldInsnNode)nodes.get(13);
                    return new ClassField("Y", f.name, f.desc);
                }
            }
        }

        return new ClassField("Y");
    }

    ClassField findZ(ClassNode node) {
        final int pattern[] = new int[]{
                Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.I2D,
                //                    Z
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.DLOAD,
                //                  SpeedZ
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DMUL, Opcodes.DSUB, Opcodes.LDC, Opcodes.DMUL,
                //                                                          AccelerationZ
                Opcodes.DLOAD, Opcodes.DLOAD, Opcodes.DMUL, Opcodes.DDIV, Opcodes.PUTFIELD
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIII)V")) {
                List<AbstractInsnNode> nodes = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                if (nodes != null) {
                    FieldInsnNode f = (FieldInsnNode)nodes.get(7);
                    return new ClassField("Z", f.name, f.desc);
                }
            }
        }
        return new ClassField("Z");
    }

    ClassField findSpeed(ClassNode node) {
        final int pattern[] = new int[]{Opcodes.INVOKESTATIC, Opcodes.PUTFIELD};

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIII)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    MethodInsnNode sqrt = (MethodInsnNode)m.instructions.get(i);
                    if (sqrt.name.equals("sqrt") && sqrt.desc.equals("(D)D")) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        return new ClassField("Speed", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Speed");
    }

    ClassField findSpeedX(ClassNode node) {
        final int pattern[] = new int[]{
                //                     X                                                       SpeedX
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.DLOAD, Opcodes.DDIV, Opcodes.PUTFIELD,
                // Nonsense
                Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.I2D,
                //                     Y                                                       SpeedY
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.DLOAD, Opcodes.DDIV, Opcodes.PUTFIELD,
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIII)V")) {
                int i = new Finder(m).findPattern(pattern, 0, false);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 5);
                    return new ClassField("SpeedX", f.name, f.desc);
                }
            }
        }

        return new ClassField("SpeedX");
    }

    ClassField findSpeedY(ClassNode node) {
        final int pattern[] = new int[]{
                //                     X                                                       SpeedX
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.DLOAD, Opcodes.DDIV, Opcodes.PUTFIELD,
                // Nonsense
                Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.I2D,
                //                     Y                                                       SpeedY
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.DLOAD, Opcodes.DDIV, Opcodes.PUTFIELD,
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIII)V")) {
                int i = new Finder(m).findPattern(pattern, 0, false);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 17);
                    return new ClassField("SpeedY", f.name, f.desc);
                }
            }
        }

        return new ClassField("SpeedY");
    }

    ClassField findSpeedZ(ClassNode node) {
        final int pattern[] = new int[]{
                Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.I2D,
                //                    Z
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.DLOAD,
                //                  SpeedZ
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DMUL, Opcodes.DSUB, Opcodes.LDC, Opcodes.DMUL,
                //                                                          AccelerationZ
                Opcodes.DLOAD, Opcodes.DLOAD, Opcodes.DMUL, Opcodes.DDIV, Opcodes.PUTFIELD
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIII)V")) {
                int i = new Finder(m).findPattern(pattern, 0, false);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 11);
                    return new ClassField("SpeedZ", f.name, f.desc);
                }
            }
        }
        return new ClassField("SpeedZ");
    }

    ClassField findAccelerationZ(ClassNode node) {
        final int pattern[] = new int[]{
                Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.I2D,
                //                    Z
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.DLOAD,
                //                  SpeedZ
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DMUL, Opcodes.DSUB, Opcodes.LDC, Opcodes.DMUL,
                //                                                          AccelerationZ
                Opcodes.DLOAD, Opcodes.DLOAD, Opcodes.DMUL, Opcodes.DDIV, Opcodes.PUTFIELD
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIII)V")) {
                int i = new Finder(m).findPattern(pattern, 0, false);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 20);
                    return new ClassField("AccelerationZ", f.name, f.desc);
                }
            }
        }
        return new ClassField("AccelerationZ");
    }

    ClassField findPitch(ClassNode node) {
        final int pattern[] = new int[]{
                //    Pitch                                          SpeedZ
                Opcodes.PUTFIELD, Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD,
                //                   Speed
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.INVOKESTATIC, Opcodes.LDC, Opcodes.DMUL,
                //                                                                         Yaw
                Opcodes.D2I, Opcodes.SIPUSH, Opcodes.IAND, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern, 0, false);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("Pitch", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("Pitch");
    }

    ClassField findYaw(ClassNode node) {
        final int pattern[] = new int[]{
                //    Pitch                                          SpeedZ
                Opcodes.PUTFIELD, Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD,
                //                   Speed
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.INVOKESTATIC, Opcodes.LDC, Opcodes.DMUL,
                //                                                                         Yaw
                Opcodes.D2I, Opcodes.SIPUSH, Opcodes.IAND, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD
        };

        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(I)V")) {
                List<AbstractInsnNode> nodes = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                if (nodes != null) {
                    if (nodes.get(14) instanceof FieldInsnNode) {
                        FieldInsnNode f = (FieldInsnNode) nodes.get(14);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Yaw", f.name, f.desc, multi);
                    }
                }
            }
        }
        return new ClassField("Yaw");
    }

    ClassField findIsMoving(ClassNode node) {
        final int pattern[] = new int[]{Opcodes.ALOAD, Opcodes.ICONST_0, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                    if (f.desc.equals("Z")) {
                        return new ClassField("IsMoving", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("isMoving");
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
}
