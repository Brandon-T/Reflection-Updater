package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Brandon on 2014-12-16.
 */
public class Typeface extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Rasteriser"))) {
                continue;
            }

            for (MethodNode m : n.methods) {
                if (m.desc.equals("(Ljava/lang/String;II)V")) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Typeface", node);
        info.putField(findCharacterPixels(node));
        info.putField(findStringWidthMethod(node));
        info.putField(findDrawStringWaveYMethod(node));
        info.putField(findDrawStringWaveXYMethod(node));
        info.putField(findDrawStringWaveAmountXYMethod(node));
        info.putField(findDrawCharMethod(node));
        info.putField(findDrawCharAlphaMethod(node));
        info.putField(findDrawCharPixelsMethod(node));
        info.putField(findDrawCharAlphaPixelsMethod(node));
        return info;
    }

    private ClassField findCharacterPixels(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[[B") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                return new ClassField("CharacterPixels", f.name, f.desc);
            }
        }
        return new ClassField("CharacterPixels");
    }

    private ClassField findStringWidthMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(Ljava/lang/String;)I") && !hasAccess(m, Opcodes.ACC_ABSTRACT)) {
                return new ClassField("*StringWidth", m.name, m.desc);
            }
        }
        return new ClassField("*StringWidth");
    }

    private ClassField findDrawStringWaveYMethod(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ALOAD, Opcodes.ALOAD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(Ljava/lang/String;IIIII)V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    return new ClassField("*DrawStringWaveY", m.name, m.desc);
                }
            }
        }
        return new ClassField("*DrawStringWaveY");
    }

    private ClassField findDrawStringWaveXYMethod(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.I2D, Opcodes.LDC, Opcodes.DDIV};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(Ljava/lang/String;IIIII)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if ((double) ((LdcInsnNode) m.instructions.get(i + 3)).cst == 3.0D) {
                        return new ClassField("*DrawStringWave", m.name, m.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("*DrawStringWaveXY");
    }

    private ClassField findDrawStringWaveAmountXYMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(Ljava/lang/String;IIIIII)V")) {
                return new ClassField("*DrawStrWaveAmt", m.name, m.desc);
            }
        }
        return new ClassField("*DrawStrWaveAmtXY");
    }

    private ClassField findDrawCharMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([BIIIII)V") && !hasAccess(m, Opcodes.ACC_ABSTRACT)) {
                return new ClassField("*DrawCharacter", m.name, m.desc);
            }
        }
        return new ClassField("DrawCharacter");
    }

    private ClassField findDrawCharAlphaMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([BIIIIII)V") && !hasAccess(m, Opcodes.ACC_ABSTRACT)) {
                return new ClassField("*DrawCharAlpha", m.name, m.desc);
            }
        }
        return new ClassField("*DrawCharAlpha");
    }

    private ClassField findDrawCharPixelsMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([I[BIIIIIII)V") && !hasAccess(m, Opcodes.ACC_ABSTRACT)) {
                return new ClassField("*DrawCharPixels", m.name, m.desc);
            }
        }
        return new ClassField("*DrawCharPixels");
    }

    private ClassField findDrawCharAlphaPixelsMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([I[BIIIIIIII)V") && !hasAccess(m, Opcodes.ACC_ABSTRACT)) {
                return new ClassField("*DrawCharPixelsA", m.name, m.desc);
            }
        }
        return new ClassField("*DrawCharPixelsA");
    }
}
