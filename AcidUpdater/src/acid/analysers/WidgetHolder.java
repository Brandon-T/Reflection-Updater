package acid.analysers;

import acid.Main;
import acid.other.DeprecatedFinder;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class WidgetHolder extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object") || !n.interfaces.isEmpty()) {
                continue;
            }

            int cache_count = 0;
            int widgets_array_count = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals(String.format("L%s;", Main.get("Cache"))) && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++cache_count;
                }

                if (f.desc.equals(String.format("[[L%s;", Main.get("Widget"))) && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++widgets_array_count;
                }
            }

            if (cache_count >= 4 && widgets_array_count == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("WidgetHolder", node.name);
        info.putField(findWidgets(node));
        return info;
    }

    private ClassField findWidgets(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[[L%s;", Main.get("Widget"))) && !hasAccess(f, Opcodes.ACC_STATIC)) {
                return new ClassField("Widgets", f.name, f.desc);
            }
        }
        return new ClassField("Widgets");
    }
}
