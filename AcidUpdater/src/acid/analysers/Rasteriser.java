package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-07.
 */
public class Rasteriser extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("CacheableNode"))) {
                continue;
            }

            int int_arr_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("[I")) {
                    ++int_arr_count;
                }
            }

            int draw_rectangle_method = 0;
            int draw_rectangle_alpha_method = 0;

            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIII)V") && hasAccess(m, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) {
                    ++draw_rectangle_method;
                }

                if (m.desc.equals("(IIIIII)V") && hasAccess(m, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) {
                    ++draw_rectangle_alpha_method;
                }
            }

            if (draw_rectangle_method >= 4 && draw_rectangle_alpha_method >= 3 && int_arr_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Rasteriser", node.name);
        info.putField(findPixels(node));
        info.putField(findWidth(node));
        info.putField(findHeight(node));
        info.putField(findCreateRasteriserMethod(node));
        info.putField(findSetCoordinatesMethod(node));
        return info;
    }

    private ClassField findPixels(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.PUTSTATIC};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([III)V") || m.desc.equals("([III[F)V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    return new ClassField("Pixels", f.name, f.desc);
                }
            }
        }
        return new ClassField("Pixels");
    }

    private ClassField findWidth(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.PUTSTATIC};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([III)V") || m.desc.equals("([III[F)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode)m.instructions.get(i)).var == 1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        return new ClassField("Width", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Width");
    }

    private ClassField findHeight(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.PUTSTATIC};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([III)V") || m.desc.equals("([III[F)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode)m.instructions.get(i)).var == 2) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        return new ClassField("Height", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Height");
    }

    private ClassField findCreateRasteriserMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([III)V") || m.desc.equals("([III[F)V")) {
                return new ClassField("*CreateRasteriser", m.name, m.desc);
            }
        }
        return new ClassField("*CreateRasteriser");
    }

    private ClassField findSetCoordinatesMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([III)V") || m.desc.equals("([III[F)V")) {
                int i = new Finder(m).findNext(0, Opcodes.INVOKESTATIC);
                if (i != -1) {
                    MethodInsnNode n = (MethodInsnNode)m.instructions.get(i);
                    return new ClassField("*SetCoordinates", n.name, n.desc);
                }
            }
        }
        return new ClassField("*SetCoordinates");
    }
}
