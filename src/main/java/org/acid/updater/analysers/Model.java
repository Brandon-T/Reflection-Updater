package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-07.
 */
public class Model extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Animable"))) {
                continue;
            }

            for (MethodNode m : n.methods) {
                //(IIIIIIIFFFIII)V
                //([[IIIIZI)Lkb;
                //(IIIIIII)V
                if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIIIIIIIJ)V")) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Model", node.name);
        info.putField(findIndicesX(node));
        info.putField(findIndicesY(node));
        info.putField(findIndicesZ(node));
        info.putField(findIndicesLength(node, info.getField("IndicesX")));
        info.putField(findVerticesX(node));
        info.putField(findVerticesY(node));
        info.putField(findVerticesZ(node));
        info.putField(findVerticesLength(node, info.getField("VerticesX")));
        info.putField(findTexturedIndicesX(node));
        info.putField(findTexturedIndicesY(node));
        info.putField(findTexturedIndicesZ(node));
        info.putField(findTextureVerticesX(node));
        info.putField(findTextureVerticesY(node));
        info.putField(findTextureVerticesZ(node));
        info.putField(findTexturedVerticesLength(node, info.getField("TexVerticesX")));
        info.putField(findVertexSkins(node));
        info.putField(findFaceColors3(node));
        info.putField(findShadowIntensity(node));
        info.putField(findFitsSingleTile(node));
        info.putField(findTranslationMethod(node));
        info.putField(findRenderAtPointMethod(node));
        return info;
    }

    private ClassField findIndicesX(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 2)).var == 1 && ((VarInsnNode)m.instructions.get(i + 4)).var == 5) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        return new ClassField("IndicesX", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("IndicesX");
    }

    private ClassField findIndicesY(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 2)).var == 1 && ((VarInsnNode)m.instructions.get(i + 4)).var == 6) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        return new ClassField("IndicesY", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("IndicesY");
    }

    private ClassField findIndicesZ(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 2)).var == 1 && ((VarInsnNode)m.instructions.get(i + 4)).var == 7) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        return new ClassField("IndicesZ", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("IndicesZ");
    }

    private ClassField findIndicesLength(ClassNode node, ClassField indices) {
        final int pattern[] = new int[]{Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals(String.format("([L%s;I)V", node.name))) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    FieldInsnNode verticesLength = (FieldInsnNode)m.instructions.get(i);
                    FieldInsnNode indicesLength = (FieldInsnNode)m.instructions.get(i + 2);
                    FieldInsnNode texVerticesLength = (FieldInsnNode)m.instructions.get(i + 4);
                    if (!verticesLength.name.equals(indicesLength.name) && !indicesLength.name.equals(texVerticesLength.name) && verticesLength.owner.equals(node.name) && indicesLength.owner.equals(node.name) && texVerticesLength.owner.equals(node.name)) {
                        return new ClassField("IndicesLength", indicesLength.name, indicesLength.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("IndicesLength");
    }

    private ClassField findVerticesX(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ILOAD, Opcodes.IMUL, Opcodes.SIPUSH, Opcodes.IDIV, Opcodes. IASTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(III)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 3)).var == 1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                        return new ClassField("VerticesX", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("VerticesX");
    }

    private ClassField findVerticesY(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ILOAD, Opcodes.IMUL, Opcodes.SIPUSH, Opcodes.IDIV, Opcodes. IASTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(III)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 3)).var == 2) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                        return new ClassField("VerticesY", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("VerticesY");
    }

    private ClassField findVerticesZ(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ILOAD, Opcodes.IMUL, Opcodes.SIPUSH, Opcodes.IDIV, Opcodes. IASTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(III)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 3)).var == 3) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                        return new ClassField("VerticesZ", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("VerticesZ");
    }

    private ClassField findVerticesLength(ClassNode node, ClassField indices) {
        final int pattern[] = new int[]{Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals(String.format("([L%s;I)V", node.name))) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    FieldInsnNode verticesLength = (FieldInsnNode)m.instructions.get(i);
                    FieldInsnNode indicesLength = (FieldInsnNode)m.instructions.get(i + 2);
                    FieldInsnNode texVerticesLength = (FieldInsnNode)m.instructions.get(i + 4);
                    if (!verticesLength.name.equals(indicesLength.name) && !indicesLength.name.equals(texVerticesLength.name) && verticesLength.owner.equals(node.name) && indicesLength.owner.equals(node.name) && texVerticesLength.owner.equals(node.name)) {
                        return new ClassField("VerticesLength", verticesLength.name, verticesLength.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("VerticesLength");
    }

    private ClassField findTexturedIndicesX(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Finder.INVOCATION};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    int VarA = ((VarInsnNode)m.instructions.get(i + 2)).var;
                    if (VarA == 1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        return new ClassField("TexIndicesX", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("TexIndicesX");
    }

    private ClassField findTexturedIndicesY(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Finder.INVOCATION};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    int VarA = ((VarInsnNode)m.instructions.get(i + 6)).var;
                    if (VarA == 1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 5);
                        return new ClassField("TexIndicesY", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("TexIndicesY");
    }

    private ClassField findTexturedIndicesZ(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Finder.INVOCATION};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    int VarA = ((VarInsnNode)m.instructions.get(i + 10)).var;
                    if (VarA == 1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 9);
                        return new ClassField("TexIndicesZ", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("TexIndicesZ");
    }

    private ClassField findTextureVerticesX(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 3)).var == 21) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                        return new ClassField("TexVerticesX", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("TexVerticesX");
    }

    private ClassField findTextureVerticesY(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 3)).var == 22) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                        return new ClassField("TexVerticesY", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("TexVerticesY");
    }

    private ClassField findTextureVerticesZ(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 3)).var == 23) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i);
                        return new ClassField("TexVerticesZ", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("TexVerticesZ");
    }

    private ClassField findTexturedVerticesLength(ClassNode node, ClassField vertices) {
        final int pattern[] = new int[]{Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals(String.format("([L%s;I)V", node.name))) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    FieldInsnNode verticesLength = (FieldInsnNode)m.instructions.get(i);
                    FieldInsnNode indicesLength = (FieldInsnNode)m.instructions.get(i + 2);
                    FieldInsnNode texVerticesLength = (FieldInsnNode)m.instructions.get(i + 4);
                    if (!verticesLength.name.equals(indicesLength.name) && !indicesLength.name.equals(texVerticesLength.name) && verticesLength.owner.equals(node.name) && indicesLength.owner.equals(node.name) && texVerticesLength.owner.equals(node.name)) {
                        return new ClassField("TexVerticesLength", texVerticesLength.name, texVerticesLength.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("TexVerticesLength");
    }

    private ClassField findVertexSkins(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(I[IIII)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((FieldInsnNode)m.instructions.get(i + 1)).desc.equals("[[I")) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        return new ClassField("Skins", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Skins");
    }

    private ClassField findFaceColors3(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.IASTORE};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals(String.format("([L%s;I)V", node.name))) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((FieldInsnNode)m.instructions.get(i + 5)).desc.equals("[I") && ((VarInsnNode)m.instructions.get(i + 6)).var == 9) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 5);
                        return new ClassField("FaceColors3", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("FaceColors3");
    }

    private ClassField findShadowIntensity(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ISTORE, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD};
        for (MethodNode m : node.methods) {
            if (m.desc.matches("\\(IIIIIIII(I|J)\\)V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 3)).var == 3) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                        return new ClassField("ShadowIntensity", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("ShadowIntensity");
    }

    private ClassField findFitsSingleTile(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LLOAD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(IIIIIIIIJ)V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                    if (f.desc.equals("Z")) {
                        return new ClassField("FitsSingleTile", f.name, f.desc);
                    }
                }
            }
        }

        pattern = new int[]{Opcodes.ALOAD, Finder.CONSTANT, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                    if (f.desc.equals("Z")) {
                        return new ClassField("FitsSingleTile", f.name, f.desc);
                    }

                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("FitsSingleTile");
    }

    private ClassField findTranslationMethod(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.DUP2, Opcodes.IALOAD, Opcodes.ILOAD, Opcodes.IADD, Opcodes.IASTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(III)V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    return new ClassField("*Translate", m.name, m.desc);
                }
            }
        }
        return new ClassField("*Translate");
    }

    private ClassField findRenderAtPointMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.matches("\\(IIIIIIII(I|J)\\)V")) {
                return new ClassField("*RenderAtPoint", m.name, m.desc);
            }
        }
        return new ClassField("*RenderAtPoint");
    }
}
