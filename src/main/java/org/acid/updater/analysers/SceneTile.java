package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Brandon on 2014-12-14.
 */
public class SceneTile extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        ClassInfo info = Main.getInfo("Scene");
        if (info != null) {
            String tile_class = info.getField("Tiles").getDesc();
            for (ClassNode n : nodes) {
                if (tile_class.equals(String.format("[[[L%s;", n.name))) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("SceneTile", node.name);
        info.putField(findBoundaryObject(node));
        info.putField(findSceneTile(node));
        info.putField(findGameObjects(node));
        info.putField(findWallDecoration(node));
        info.putField(findFloorDecoration(node));
        info.putField(findX(node));
        info.putField(findY(node));
        info.putField(findPlane(node));

        Main.getInfo("Scene").setField(findRegionTiles(node));
        return info;
    }

    private ClassField findBoundaryObject(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("BoundaryObject")))) {
                return new ClassField("BoundaryObject", f.name, f.desc);
            }
        }
        return new ClassField("BoundaryObject");
    }

    private ClassField findSceneTile(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", node.name))) {
                return new ClassField("SceneTile", f.name, f.desc);
            }
        }
        return new ClassField("SceneTile");
    }

    private ClassField findGameObjects(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[L%s;", Main.get("GameObject")))) {
                return new ClassField("GameObjects", f.name, f.desc);
            }
        }
        return new ClassField("GameObjects");
    }

    private ClassField findWallDecoration(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("WallDecoration")))) {
                return new ClassField("WallDecoration", f.name, f.desc);
            }
        }
        return new ClassField("WallDecoration");
    }

    private ClassField findFloorDecoration(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("FloorDecoration")))) {
                return new ClassField("FloorDecoration", f.name, f.desc);
            }
        }
        return new ClassField("FloorDecoration");
    }

    private ClassField findX(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 1)).var == 2) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("X", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("X");
    }

    private ClassField findY(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 1)).var == 3) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Y", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Y");
    }

    private ClassField findPlane(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.PUTFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("Plane", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("Plane");
    }

    private ClassField findRegionTiles(ClassNode node) {
        for (FieldNode f : Main.getClassNode("Scene").fields) {
            if (f.desc.equals(String.format("[[[L%s;", node.name)) && !hasAccess(f, Opcodes.ACC_STATIC)) {
                return new ClassField("Tiles", f.name, f.desc);
            }
        }
        return new ClassField("Tiles");
    }
}
