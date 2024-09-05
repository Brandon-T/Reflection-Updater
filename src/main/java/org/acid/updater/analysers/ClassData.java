package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-23.
 */
public class ClassData extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Node"))) {
                continue;
            }

            int byte_arr_3D_count = 0, method_count = 0, field_count = 0;
            for (FieldNode f : n.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[[[B")) {
                    ++byte_arr_3D_count;
                } else if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[Ljava/lang/reflect/Method;")) {
                    ++method_count;
                } else if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[Ljava/lang/reflect/Field;")) {
                    ++field_count;
                }
            }

            if (byte_arr_3D_count >= 1 && method_count >= 1 && field_count >= 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("ClassData", node.name);
        info.putField(findBytes(node));
        info.putField(findMethods(node));
        info.putField(findFields(node));
        return info;
    }

    private ClassField findBytes(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[[[B")) {
                return new ClassField("Bytes", f.name, f.desc);
            }
        }
        return new ClassField("Bytes");
    }

    private ClassField findMethods(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[Ljava/lang/reflect/Method;")) {
                return new ClassField("Methods", f.name, f.desc);
            }
        }
        return new ClassField("Methods");
    }

    private ClassField findFields(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[Ljava/lang/reflect/Field;")) {
                return new ClassField("Fields", f.name, f.desc);
            }
        }
        return new ClassField("Fields");
    }
}
