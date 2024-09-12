package org.acid.updater.deobfuscator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Brandon on 2015-01-11.
 */
public class ParameterRemover extends Deobfuscator {
    private final ArrayList<Info> info;
    private int total_count = 0, removal_count = 0;

    public ParameterRemover(Collection<ClassNode> classes) {
        super(classes);
        this.info = new ArrayList<>();
    }

    @Override
    public Deobfuscator analyse() {
        classes.stream().forEach(c -> c.methods.forEach(m -> countUnusedParameters(c, m)));
        return this;
    }

    @Override
    public void remove() {
        info.stream().forEach(i -> removeUnusedParameters(i.getNode(), i.getMethod()));
        this.info.clear();

        int temp = removal_count;
        while (removal_count > 0) {
            removal_count = 0;
            this.analyse();
            info.stream().forEach(i -> removeUnusedParameters(i.getNode(), i.getMethod()));
            this.info.clear();

            temp += removal_count;
        }

        removal_count = temp;
        System.out.println("Removed Parameters: " + removal_count + " of " + total_count);
    }

    public void countUnusedParameters(ClassNode node, MethodNode method) {
        if ((method.access & Opcodes.ACC_ABSTRACT) == 0) {
            Type[] types = Type.getArgumentTypes(method.desc);

            if (types.length > 0 && types[types.length - 1].toString().matches("I|S|B|Z")) {
                int lastParameterCount = getLastParameterCount(method);
                for (int i = 0; i < method.instructions.size(); ++i) {
                    if (method.instructions.get(i) instanceof VarInsnNode) {
                        int v = ((VarInsnNode) method.instructions.get(i)).var;
                        if (v == lastParameterCount) {
                            return;
                        }
                    }
                }

                if (!info.contains(new Info(node, method))) {
                    info.add(new Info(node, method));
                }

                ++total_count;
            }
        }
    }

    public void removeUnusedParameters(ClassNode node, MethodNode method) {
        final boolean[] removed = {false};
        String old = method.desc;
        String desc = getNewDescription(method);

        classes.stream().forEach(c -> {
            c.methods.stream().forEach(m -> {
                for (int i = 0; i < m.instructions.size(); ++i) {
                    if (m.instructions.get(i) instanceof MethodInsnNode mi) {
                        if (!mi.owner.startsWith("java")) {
                            if (mi.owner.equals(node.name) && mi.name.equals(method.name) && mi.desc.equals(old)) {
                                int j = countMultiplierInstructions(m.instructions.toArray(), i - 1);
                                if (j > 0) {
                                    for (int k = 0; k < j; ++k) {
                                        m.instructions.remove(m.instructions.get(i - k - 1));
                                        mi.desc = desc;
                                        method.desc = desc;
                                        removed[0] = true;
                                    }
                                } else {
                                    System.out.println("Failed to remove: " + c.name + " -> " + mi.name + " -> " + mi.desc);
                                }
                            }
                        }
                    }
                }
            });
        });

        if (removed[0]) {
            ++removal_count;
        }
    }

    private String getNewDescription(MethodNode method) {
        Type[] types = Type.getArgumentTypes(method.desc);

        if (types.length > 0) {
            if (types[types.length - 1].getDescriptor().matches("I|S|B|Z")) {
                StringBuilder builder = new StringBuilder();
                builder.append("(");
                for (int i = 0; i < types.length - 1; ++i) {
                    builder.append(types[i].getDescriptor());
                }
                builder.append(")").append(Type.getReturnType(method.desc).getDescriptor());
                return builder.toString();
            }
        }

        return method.desc;

//        int last_param_index = method.desc.lastIndexOf(types[types.length - 1].toString() + ")");
//        int last_bracket_index = method.desc.indexOf(')');
//        return method.desc.substring(0, last_param_index) + method.desc.substring(last_bracket_index);
    }

    private boolean canRemove(int opcode) {
        return (opcode >= Opcodes.LDC && opcode <= Opcodes.DLOAD) || (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.DCONST_1) || (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH);
    }

    private int countMultiplierInstructions(AbstractInsnNode[] instructions, int index) {
        int i = 0;
        int opcode = instructions[index].getOpcode();

        if (canRemove(opcode)) {
            return 1;
        }

        if (opcode >= Opcodes.IADD && opcode <= Opcodes.DDIV) {
            while (i < 5) {
                if (instructions[index].getOpcode() == Opcodes.GETSTATIC) {
                    return i + 1;
                } else if (instructions[index].getOpcode() == Opcodes.GETFIELD) {
                    while (i < 10) {
                        if (instructions[index].getOpcode() == Opcodes.ALOAD) {
                            return i + 1;
                        }
                        --index;
                        ++i;
                    }
                }
                --index;
                ++i;
            }
        }
        return 0;
    }

    private int getLastParameterCount(MethodNode method) {
        Type[] types = Type.getArgumentTypes(method.desc);
        int i = (method.access & Opcodes.ACC_STATIC) == 0 ? 0 : -1;
        for (Type t : types) {
            switch (t.toString()) {
                case "J":
                case "D":
                    ++i;
                default:
                    ++i;
                    break;
            }
        }
        return i;
    }

    private class Info {
        private final ClassNode node;
        private final MethodNode method;

        public Info(ClassNode node, MethodNode method) {
            this.node = node;
            this.method = method;
        }

        public ClassNode getNode() {
            return node;
        }

        public MethodNode getMethod() {
            return method;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Info info) {
                return node.name.equals(info.node.name) && method.name.equals(info.method.name) && method.desc.equals(info.method.desc);
            }
            return false;
        }

        @Override
        public String toString() {
            return node.name + "." + method.name + "   " + method.desc;
        }
    }
}