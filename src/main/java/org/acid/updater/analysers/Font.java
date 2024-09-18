package org.acid.updater.analysers;

import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public class Font extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (n.superName.equals("java/lang/Object")) {
                continue;
            }

            int constructor_count = 0, drawGlyph_count = 0, drawGlyphAlpha_count = 0;
            for (MethodNode m : n.methods) {
                if (m.name.equals("<init>")) {
                    if (m.desc.equals("([B[I[I[I[I[I[[B)V")) {
                        constructor_count += 1;
                    } else if (m.desc.equals("([B)V")) {
                        constructor_count += 1;
                    }
                } else if (m.desc.equals("([BIIIII)V") && !hasAccess(m, Opcodes.ACC_STATIC) && hasAccess(m, Opcodes.ACC_FINAL)) {
                    drawGlyph_count += 1;
                } else if (m.desc.equals("([BIIIIII)V") && !hasAccess(m, Opcodes.ACC_STATIC) && hasAccess(m, Opcodes.ACC_FINAL)) {
                    drawGlyphAlpha_count += 1;
                }
            }

            if (constructor_count == 2 && drawGlyph_count >= 1 && drawGlyphAlpha_count >= 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Font", node);
        info.putField(findDrawGlyphsMethod(node));
        info.putField(findDrawGlyphsAlphaMethod(node));
        return info;
    }

    private ClassField findDrawGlyphsMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([BIIIII)V") && !hasAccess(m, Opcodes.ACC_STATIC) && hasAccess(m, Opcodes.ACC_FINAL)) {
                return new ClassField("*drawGlyphs", m.name, m.desc);
            }
        }
        return new ClassField("*drawGlyphs");
    }

    private ClassField findDrawGlyphsAlphaMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("([BIIIIII)V") && !hasAccess(m, Opcodes.ACC_STATIC) && hasAccess(m, Opcodes.ACC_FINAL)) {
                return new ClassField("*drawGlyphsAlpha", m.name, m.desc);
            }
        }
        return new ClassField("*drawGlyphsAlpha");
    }
}
