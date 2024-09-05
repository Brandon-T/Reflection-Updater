package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-16.
 */
public class Rasteriser3D extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            int arr = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("[I") && hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++arr;
                }
            }

            boolean found = false;
            method: for (MethodNode m : n.methods) {
                int[] pattern = new int[]{Opcodes.LDC, Opcodes.ILOAD, Opcodes.I2D};
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    double value = (Double)((LdcInsnNode)m.instructions.get(i)).cst;
                    if ((long)value == 65536) {
                        found = true;
                        break method;
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }

            if (arr >= 4 && found) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Rasteriser3D", node.name);
        info.putField(findShadowDecay(node));
        info.putField(findSineTable(node));
        info.putField(findCosTable(node));
        return info;
    }

    private ClassField findShadowDecay(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.LDC, Opcodes.ILOAD, Opcodes.IDIV, Opcodes.IASTORE};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if ((int) ((LdcInsnNode)m.instructions.get(i + 2)).cst == 0x8000) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                        return new ClassField("ShadowDecay", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("ShadowDecay");
    }

    private ClassField findSineTable(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>")) {
                int i = new Finder(m).findNext(0, Opcodes.INVOKESTATIC);
                while (i != -1) {
                    MethodInsnNode n = (MethodInsnNode)m.instructions.get(i);
                    if (n.name.equals("sin") && n.owner.equals("java/lang/Math")) {
                        int j = new Finder(m).findPrev(i, Opcodes.GETSTATIC);
                        if (j != -1) {
                            FieldInsnNode f = (FieldInsnNode)m.instructions.get(j);
                            return new ClassField("SineTable", f.name, f.desc);
                        }
                    }
                    i = new Finder(m).findNext(i + 1, Opcodes.INVOKESTATIC);
                }
            }
        }
        return new ClassField("SineTable");
    }

    private ClassField findCosTable(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>")) {
                int i = new Finder(m).findNext(0, Opcodes.INVOKESTATIC);
                while (i != -1) {
                    MethodInsnNode n = (MethodInsnNode)m.instructions.get(i);
                    if (n.name.equals("cos") && n.owner.equals("java/lang/Math")) {
                        int j = new Finder(m).findPrev(i, Opcodes.GETSTATIC);
                        if (j != -1) {
                            FieldInsnNode f = (FieldInsnNode)m.instructions.get(j);
                            return new ClassField("CosineTable", f.name, f.desc);
                        }
                    }
                    i = new Finder(m).findNext(i + 1, Opcodes.INVOKESTATIC);
                }
            }
        }
        return new ClassField("CosineTable");
    }
}
