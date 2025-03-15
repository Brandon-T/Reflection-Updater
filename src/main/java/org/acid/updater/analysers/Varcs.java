package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public class Varcs extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object")) {
                continue;
            }

            int long_count = 0;
            int boolean_count = 0;
            int boolean_array_count = 0;
            int string_array_count = 0;
            int map_count = 0;
            int method_get_int_count = 0;
            int method_get_string_count = 0;

            for (FieldNode f : n.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("J")) {
                    ++long_count;
                }

                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("Z")) {
                    ++boolean_count;
                }

                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[Z")) {
                    ++boolean_array_count;
                }

                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[Ljava/lang/String;")) {
                    ++string_array_count;
                }

                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("Ljava/util/Map;")) {
                    ++map_count;
                }
            }

            for (MethodNode m : n.methods) {
                if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(I)I")) {
                    ++method_get_int_count;
                }

                if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(I)Ljava/lang/String;")) {
                    ++method_get_string_count;
                }
            }

            if (method_get_string_count > 0 && method_get_int_count > 0 &&
                    long_count == 1 && boolean_count == 1 &&
                    boolean_array_count == 1 &&
                    map_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Varcs", node);
        info.putField(findMap(node));
        info.putField(findStrings(node));
        info.putField(findVarcMap(node));
        return info;
    }

    private ClassField findMap(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("Ljava/util/Map;")) {
                return new ClassField("Map", f.name, f.desc);
            }
        }
        return new ClassField("Map");
    }

    private ClassField findStrings(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[Ljava/lang/String;")) {
                return new ClassField("Strings", f.name, f.desc);
            }
        }
        return new ClassField("Strings");
    }

    private ClassField findVarcMap(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (FieldNode f : n.fields) {
                if (hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("L%s;", node.name))) {
                    return new ClassField("VarcMap", n.name, f.name, f.desc);
                }
            }
        }
        return new ClassField("VarcMap");
    }
}
