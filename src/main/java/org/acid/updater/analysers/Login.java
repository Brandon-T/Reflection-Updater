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

public class Login extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode node : nodes) {
            if (!node.superName.equals("java/lang/Object")) {
                continue;
            }

            int indexedRGB_count = 0, string_count = 0, string_array_count = 0, bool_count = 0;
            String indexedRGBField = Main.get("IndexedRGB");

            for (FieldNode field : node.fields) {
                if (hasAccess(field, Opcodes.ACC_STATIC) && field.desc.equals(String.format("L%s;", indexedRGBField))) {
                    ++indexedRGB_count;
                }

                if (hasAccess(field, Opcodes.ACC_STATIC) && field.desc.equals("Ljava/lang/String;")) {
                    ++string_count;
                }

                if (hasAccess(field, Opcodes.ACC_STATIC) && field.desc.equals("[Ljava/lang/String;")) {
                    ++string_array_count;
                }

                if (hasAccess(field, Opcodes.ACC_STATIC) && field.desc.equals("Z")) {
                    ++bool_count;
                }
            }

            if (indexedRGB_count >= 2 && string_count >= 4 && string_array_count >= 2 && bool_count >= 4) {
                return node;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Login", node.name);
        info.putField(findField(node, "XPadding", 0));
        info.putField(findField(node, "BoxXOffset", 1));
        info.putField(findField(node, "LoadingPercent", 2));
        info.putField(findField(node, "AccountStatus", 4));
        info.putField(findField(node, "Index", 6));
        info.putField(findButtonSprite(node));
        info.putField(findField(node, "Username", 11));
        info.putField(findField(node, "Password", 12));
        info.putField(findField(node, "CursorField", 16));

        return info;
    }

    private ClassField findField(ClassNode node, String name, int index) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTSTATIC, index);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField(name, f.name, f.desc, multi);
                }
            }
        }
        return new ClassField(name);
    }

    private ClassField findButtonSprite(ClassNode node) {
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.BIPUSH, Opcodes.ISUB, Opcodes.ILOAD};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                String fontName = Main.get("Font");
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;L%s;L%s;)V", fontName, fontName, fontName))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("ButtonSprite", f.owner, f.name, f.desc, multi);
                    }
                }
            }
        }
        return new ClassField("ButtonSprite");
    }
}
