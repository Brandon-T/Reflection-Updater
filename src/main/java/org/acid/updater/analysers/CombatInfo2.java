package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Brandon on 2017-04-29.
 */
public class CombatInfo2 extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode node : nodes) {
            if (!node.superName.equals(Main.get("CacheableNode"))) {
                continue;
            }

            int int_count = 0, short_arr_count = 0, str_count = 0, str_arr_count = 0, method_count = 0;
            for (FieldNode field : node.fields) {
                if (field.desc.equals("I") && !hasAccess(field, Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)) {
                    ++int_count;
                } else if (field.desc.equals("[S") && !hasAccess(field, Opcodes.ACC_STATIC)) {
                    ++short_arr_count;
                } else if (field.desc.equals("Ljava/lang/String;") && !hasAccess(field, Opcodes.ACC_STATIC)) {
                    ++str_count;
                } else if (field.desc.equals("[Ljava/lang/String;") && !hasAccess(field, Opcodes.ACC_STATIC)) {
                    ++str_arr_count;
                }
            }

            for (MethodNode method : node.methods) {
                if (!hasAccess(method, Opcodes.ACC_STATIC) && method.desc.equals(String.format("(L%s;I)V", Main.get("Buffer")))) {
                    ++method_count;
                }
            }

            if (int_count >= 10 && short_arr_count == 0 && str_count == 0 && str_arr_count == 0 && method_count >= 1) {
                return node;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo classInfo = new ClassInfo("CombatInfo2", node.name);
        //classInfo.putField(findHealthScale(node)); Found in Entity Analyser.
        return classInfo;
    }
}
