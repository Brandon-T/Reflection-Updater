package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.DeprecatedFinder;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.List;

public class Menu extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode node : nodes) {
            if (!node.superName.equals("java/lang/Object")) {
                continue;
            }

            int int_count = 0;
            int long_count = 0;
            int string_array_count = 0;
            int int_array_count = 0;
            int menus_array_count = 0;

            for (FieldNode f : node.fields) {
                if (!hasAccess(f, Opcodes.ACC_STATIC)) {
                    if (f.desc.equals("I")) {
                        ++int_count;
                    } else if (f.desc.equals("J")) {
                        ++long_count;
                    } else if (f.desc.equals("[Ljava/lang/String;")) {
                        ++string_array_count;
                    } else if (f.desc.equals("[I")) {
                        ++int_array_count;
                    } else if (f.desc.equals(String.format("[L%s;", node.name))) {
                        ++menus_array_count;
                    }
                }
            }

            if (int_count >= 4 && string_array_count == 2 && long_count == 1 && int_array_count >= 4 && menus_array_count == 1) {
                return node;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Menu", node);
        info.putField(findMenuCount(node));
        info.putField(findMenuActions(node));
        info.putField(findMenuOptions(node));
        info.putField(findMenuX(node));
        info.putField(findMenuY(node));
        info.putField(findMenuWidth(node));
        info.putField(findMenuHeight(node));
        return info;
    }

    private ClassField findMenuCount(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Finder.COMPARISON};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(II)I")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                    long multi = (int) ((LdcInsnNode) m.instructions.get(i + 3)).cst;
                    return new ClassField("Count", f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("Count");
    }

    private ClassField findMenuActions(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.AALOAD, Opcodes.ARETURN};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)Ljava/lang/String;")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    if (f.desc.equals("[Ljava/lang/String;")) {
                        return new ClassField("Actions", f.owner, f.name, f.desc);
                    }
                }
            }
        }

        return new ClassField("Actions");
    }

    private ClassField findMenuOptions(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.AALOAD, Opcodes.INVOKEVIRTUAL, Finder.COMPARISON2};

        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)Ljava/lang/String;")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    if (f.desc.equals("[Ljava/lang/String;")) {
                        return new ClassField("Options", f.owner, f.name, f.desc);
                    }
                }
            }
        }

        return new ClassField("Options");
    }

    private ClassField findMenuX(ClassNode node) {
        final int[] pattern = new int[]{
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu X
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Y
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Width
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE //Menu Height
        };
        for (MethodNode m : node.methods) {
            if (m.desc.equals("()V")) {
                List<AbstractInsnNode> insns = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                if (insns != null) {
                    FieldInsnNode f = (FieldInsnNode) insns.get(1);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("X", f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("X");
    }

    private ClassField findMenuY(ClassNode node) {
        final int[] pattern = new int[]{
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu X
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Y
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Width
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE //Menu Height
        };
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()V")) {
                List<AbstractInsnNode> insns = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                if (insns != null) {
                    FieldInsnNode f = (FieldInsnNode) insns.get(6);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("Y", f.owner, f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("Y");
    }

    private ClassField findMenuWidth(ClassNode node) {
        final int[] pattern = new int[]{
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu X
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Y
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Width
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE //Menu Height
        };
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()V")) {
                List<AbstractInsnNode> insns = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                if (insns != null) {
                    FieldInsnNode f = (FieldInsnNode) insns.get(11);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("Width", f.owner, f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("Width");
    }

    private ClassField findMenuHeight(ClassNode node) {
        final int[] pattern = new int[]{
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu X
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Y
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Width
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE //Menu Height
        };
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()V")) {
                List<AbstractInsnNode> insns = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                if (insns != null) {
                    FieldInsnNode f = (FieldInsnNode) insns.get(16);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("Height", f.owner, f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("Height");
    }
}
