package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public class VarbitDefinition extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("CacheableNode")) || !n.interfaces.isEmpty()) {
                continue;
            }

            int int_count = 0;
            int cache_count = 0;
            int method_stream_int_count = 0;
            int method_stream_count = 0;

            for (FieldNode f : n.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("I")) {
                    ++int_count;
                }

                if (hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("L%s;", Main.get("Cache")))) {
                    ++cache_count;
                }
            }

            for (MethodNode m : n.methods) {
                if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;I)V", Main.get("Buffer")))) {
                    ++method_stream_int_count;
                }

                if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;)V", Main.get("Buffer")))) {
                    ++method_stream_count;
                }
            }

            if (int_count == 3 && cache_count == 1 && method_stream_int_count == 1 && method_stream_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("VarbitDefinition", node);
        info.putField(findCache(node));
        info.putField(findBase(node));
        info.putField(findStartBit(node));
        info.putField(findEndBit(node));
        return info;
    }

    private ClassField findCache(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("L%s;", Main.get("Cache")))) {
                return new ClassField("Cache", f.name, f.desc);
            }
        }
        return new ClassField("Cache");
    }

    private ClassField findBase(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;I)V", Main.get("Buffer")))) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 0);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("Base", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("Base");
    }

    private ClassField findStartBit(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;I)V", Main.get("Buffer")))) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 1);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("StartBit", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("StartBit");
    }

    private ClassField findEndBit(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;I)V", Main.get("Buffer")))) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 2);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("EndBit", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("EndBit");
    }
}
