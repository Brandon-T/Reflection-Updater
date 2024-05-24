package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-16.
 */
public class ImageRGB extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode node : nodes) {
            if (!node.superName.equals(Main.get("Rasteriser"))) {
                continue;
            }

            int method_count = 0, component_method_count = 0;
            for (MethodNode m : node.methods) {
                if (m.name.equals("<init>") && m.desc.equals("([BLjava/awt/Component;)V")) {
                    ++component_method_count;
                }
                else if (m.name.equals("<init>") && m.desc.equals("(II)V")) {
                    ++method_count;
                }
                else if (m.name.equals("<init>") && m.desc.equals("([III)V")) {
                    ++method_count;
                }
            }

            if (component_method_count >= 1 || method_count >= 2) {
                return node;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("ImageRGB", node.name);
        info.putField(findPixels(node));
        info.putField(findWidth(node));
        info.putField(findHeight(node));
        info.putField(findMaxWidth(node));
        info.putField(findMaxHeight(node));
        info.putField(findCopyPixelsMethod(node));
        info.putField(findCopyPixelsAlphaMethod(node));
        info.putField(findShapeImagePixelsMethod(node));
        return info;
    }

    private ClassField findPixels(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("([III)V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                    return new ClassField("Pixels", f.name, f.desc);
                }
            }
        }
        return new ClassField("Pixels");
    }

    private ClassField findWidth(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.DUP_X1, Opcodes.PUTFIELD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("([III)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i)).var == 2) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 3);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Width", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Width");
    }

    private ClassField findHeight(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.DUP_X1, Opcodes.PUTFIELD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("([III)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i)).var == 3) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 3);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Height", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Height");
    }

    private ClassField findMaxWidth(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.DUP_X1, Opcodes.PUTFIELD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("([III)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i)).var == 2) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("MaxWidth", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("MaxWidth");
    }

    private ClassField findMaxHeight(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.DUP_X1, Opcodes.PUTFIELD, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("([III)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i)).var == 3) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("MaxHeight", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("MaxHeight");
    }

    private ClassField findCopyPixelsMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([I[IIIIIII)V")) {
                return new ClassField("*CopyPixels", m.name, m.desc);
            }
        }
        return new ClassField("*CopyPixels");
    }

    private ClassField findCopyPixelsAlphaMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([I[IIIIIIIII)V")) {
                return new ClassField("*CopyPixelsA", m.name, m.desc);
            }
        }
        return new ClassField("*CopyPixelsA");
    }

    private ClassField findShapeImagePixelsMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([I[IIIIIIII)V")) {
                return new ClassField("*ShapeImage", m.name, m.desc);
            }
        }
        return new ClassField("*ShapeImage");
    }
}
