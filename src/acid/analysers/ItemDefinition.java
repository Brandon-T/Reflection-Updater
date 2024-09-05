package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-15.
 */
public class ItemDefinition extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("CacheableNode"))) {
                continue;
            }

            int short_arr_count = 0, str_arr_count = 0, int_arr_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("[S")) {
                    ++short_arr_count;
                } else if (f.desc.equals("[I")) {
                    ++int_arr_count;
                } else if (f.desc.equals("[Ljava/lang/String;")) {
                    ++str_arr_count;
                }
            }

            if (short_arr_count >= 4 && int_arr_count >= 2 && str_arr_count >= 2) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("ItemDefinition", node.name);
        info.putField(findID(node));
        info.putField(findName(node));
        info.putField(findIsMembers(node));
        info.putField(findGroundActions(node));
        info.putField(findActions(node, info.getField("GroundActions")));
        info.putField(findCache(node));
        return info;
    }

    private ClassField findID(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.I2L};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(I)L%s;", Main.get("Model")))) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    long multi = (int) ((LdcInsnNode)m.instructions.get(i + 3)).cst;
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                    return new ClassField("ID", f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("ID");
    }

    private ClassField findName(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                for (AbstractInsnNode a : m.instructions.toArray()) {
                    if (a instanceof FieldInsnNode) {
                        FieldInsnNode f = (FieldInsnNode)a;
                        if (f.owner.equals(node.name) && f.desc.equals("Ljava/lang/String;")) {
                            return new ClassField("Name", f.name, f.desc);
                        }
                    }
                }
            }
        }
        return new ClassField("Name");
    }

    private ClassField findIsMembers(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;L%s;)V", node.name, node.name))) {
                for (AbstractInsnNode a : m.instructions.toArray()) {
                    if (a instanceof FieldInsnNode) {
                        FieldInsnNode f = (FieldInsnNode)a;
                        if (f.desc.equals("Z") && f.owner.equals(node.name)) {
                            return new ClassField("IsMembers", f.name, f.desc);
                        }
                    }
                }
            }
        }
        return new ClassField("IsMembers");
    }

    private ClassField findGroundActions(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;I)V", Main.get("Buffer")))) {
                int i = new Finder(m).findNext(0, Opcodes.INVOKEVIRTUAL);
                while (i != -1) {
                    if (((MethodInsnNode)m.instructions.get(i)).name.contains("equals")) {
                        int j = new Finder(m).findPrev(i, Opcodes.GETFIELD);
                        while (j != -1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                            if (f.desc.equals("[Ljava/lang/String;")) {
                                return new ClassField("GroundActions", f.name, f.desc);
                            }
                            j = new Finder(m).findPrev(j - 1, Opcodes.GETFIELD);
                        }
                    }
                    i = new Finder(m).findNext(i + 1, Opcodes.INVOKEVIRTUAL);
                }
            }
        }
        return new ClassField("GroundActions");
    }

    private ClassField findActions(ClassNode node, ClassField actions) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;I)V", Main.get("Buffer")))) {
                for (AbstractInsnNode a : m.instructions.toArray()) {
                    if (a instanceof FieldInsnNode) {
                        FieldInsnNode f = (FieldInsnNode)a;
                        if (f.owner.equals(node.name) && f.desc.equals("[Ljava/lang/String;") && !f.name.equals(actions.getName())) {
                            return new ClassField("Actions", f.name, f.desc);
                        }
                    }
                }
            }
        }
        return new ClassField("Actions");
    }

    private ClassField findCache(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.I2L, Opcodes.INVOKEVIRTUAL};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(I)L%s;", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode)m.instructions.get(i + 1)).var == 0) {
                            FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                            return new ClassField("Cache", f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        for (MethodNode m : node.methods) {
            if (m.desc.equals("<clinit>")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTSTATIC, 0);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                    return new ClassField("Cache", f.name, f.desc);
                }
            }
        }

        return new ClassField("Cache");
    }
}
