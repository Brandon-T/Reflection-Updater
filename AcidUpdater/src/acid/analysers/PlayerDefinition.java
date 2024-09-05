package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Created by Kira on 2014-12-08.
 */
public class PlayerDefinition extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            int bool_count = 0, int_arr_count = 0, long_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("Z")) {
                    ++bool_count; // Is Female
                } else if (f.desc.equals("[I")) {
                    ++int_arr_count;
                } else if (f.desc.equals("J")) {
                    ++long_count; // Model ID
                }
            }

            if (bool_count >= 1 && int_arr_count >= 3 && long_count == 2) {
                for (MethodNode m : n.methods) {
                    if (m.desc.equals("(IZ)V")) {
                        return n;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("PlayerDefinition", node.name);
        info.putField(findNpcTransformID(node));
        info.putField(findGender(node));
        info.putField(findAnimatedModelID(node));
        info.putField(findModelID(node));
        info.putField(findEquipment(node));
        info.putField(findModelCache(node));
        return info;
    }

    private ClassField findNpcTransformID(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("()I")) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL});
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                    return new ClassField("NpcTransformID", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("NpcTransformID");
    }

    private ClassField findGender(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                final int pattern[] = new int[]{Opcodes.ALOAD, Opcodes.ICONST_0, Opcodes.PUTFIELD};
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((FieldInsnNode) m.instructions.get(i + 2)).desc.equals("I")) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        return new ClassField("Gender", f.name, f.desc);
                    }

                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }

        return new ClassField("Gender");
    }

    private ClassField findAnimatedModelID(ClassNode node) {
        for (MethodNode m : node.methods) {
            int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.LMUL, Opcodes.LSTORE});
            if (i != -1) {
                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                long multi = (long) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                return new ClassField("AnimatedModelID", f.name, f.desc, multi);
            }
        }
        return new ClassField("AnimatedModelID");
    }

    private ClassField findModelID(ClassNode node) {
        for (MethodNode m : node.methods) {
            int i = new Finder(m).findPattern(new int[]{Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.LMUL, Opcodes.INVOKEVIRTUAL, Opcodes.CHECKCAST, Opcodes.ASTORE});
            if (i != -1) {
                long multi = (long) ((LdcInsnNode)m.instructions.get(i + 3)).cst;
                FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                return new ClassField("ModelID", f.name, f.desc, multi);
            }
        }
        return new ClassField("ModelID");
    }

    public ClassField findEquipment(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals(String.format("(L%s;)V", node.name))) {
                int pattern[] = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ARRAYLENGTH};
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    return new ClassField("Equipment", f.name, f.desc);
                }
            }
        }
        return new ClassField("Equipment");
    }

    private ClassField findModelCache(ClassNode node) {
        final int pattern[] = new int[]{Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC};
        String desc = String.format("(L%s;IL%s;I)L%s;", Main.get("AnimationSequence"), Main.get("AnimationSequence"), Main.get("Model"));
        for (MethodNode m : node.methods) {
            if (m.desc.equals(desc)) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((FieldInsnNode)m.instructions.get(i)).desc.equals(String.format("L%s;", Main.get("Cache")))) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        return new ClassField("ModelCache", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("ModelCache");
    }
}
