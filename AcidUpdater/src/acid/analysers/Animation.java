package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.Collection;

public class Animation extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        ClassNode node = Main.getClassNode("AnimationFrames");
        if (node != null) {
            for (FieldNode f : node.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL) && Type.getType(f.desc).getSort() == Type.ARRAY) {
                    for (ClassNode n : nodes) {
                        if (n.name.equals(Type.getType(f.desc).getElementType().getClassName())) {
                            return n;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Animation", node.name);
        Arrays.stream(fieldFields(node)).forEachOrdered(info::putField);
        return info;
    }

    private ClassField[] fieldFields(ClassNode node) {
        ClassField[] fields = {
                new ClassField("FrameCount"),
                new ClassField("TransformY"),
                new ClassField("TransformX"),
                new ClassField("Frames"),
                new ClassField("TransformZ"),
                new ClassField("Skeleton")
        };

        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.PUTFIELD};
        int[] pattern2 = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.NEWARRAY, Opcodes.PUTFIELD};

        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("AnimationSkeleton")))) {
                fields[5] = new ClassField(fields[5].getId(), f.name, f.desc);
                break;
            }
        }

        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern, 0, false);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 1)).var == 7 && ((FieldInsnNode)m.instructions.get(i + 2)).desc.equals("I")) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        fields[0] = new ClassField(fields[0].getId(), f.name, f.desc, multi);
                        break;
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }

                if (i == -1) {
                    return fields;
                }

                i = 0;
                for (int j = 1; j < 5; ++j) {
                    i = new Finder(m).findPattern(pattern2, i + 1, false);
                    if (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 1)).var == 7 && ((FieldInsnNode) m.instructions.get(i + 3)).desc.equals("[I")) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                            long multi = Main.findMultiplier(f.owner, f.name);
                            fields[j] = new ClassField(fields[j].getId(), f.name, f.desc, multi);
                        }
                    }
                }
            }
        }
        return fields;
    }
}