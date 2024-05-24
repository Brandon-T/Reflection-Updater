package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

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
        Main.getInfo("Widget").setField(findWidgetSpriteID(node));
        Main.getInfo("Widget").setField(findWidgetTextureID(node));
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

    private ClassField findWidgetSpriteID(ClassNode node) {
        if (Main.getInfo("Widget").getField("SpriteID").getName().equals("N/A")) {
            final int pattern[] = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_M1};

            for (MethodNode m : Main.getClassNode("Widget").methods) {
                if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;Z)L%s;", node.name, Main.get("SpriteMask")))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        int multi = (int)((LdcInsnNode)m.instructions.get(i + 2)).cst;
                        return new ClassField("SpriteID", f.name, f.desc, multi);
                    }
                }
            }
        }
        return Main.getInfo("Widget").getField("SpriteID");
    }

    private ClassField findWidgetTextureID(ClassNode node) {
        if (Main.getInfo("Widget").getField("TextureID").getName().equals("N/A")) {
            final int pattern[] = new int[]{Opcodes.ILOAD, Opcodes.IFEQ, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL};

            for (MethodNode m : Main.getClassNode("Widget").methods) {
                if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;Z)L%s;", node.name, Main.get("SpriteMask")))) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    while (i != -1) {
                        int j = new Finder(m).findNext(i + 3, Opcodes.GETFIELD, false);
                        if (j != -1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                            int multi = (int) ((LdcInsnNode) m.instructions.get(j + 1)).cst;
                            return new ClassField("TextureID", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return Main.getInfo("Widget").getField("TextureID");
    }
}
