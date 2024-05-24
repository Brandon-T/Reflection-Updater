package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-14.
 */
public class SceneTile extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        ClassInfo info = Main.getInfo("Region");
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
        info.putField(findBoundary(node));
        info.putField(findSceneTile(node));
        info.putField(findInteractables(node));
        info.putField(findWallDecoration(node));
        info.putField(findGroundDecoration(node));
        info.putField(findX(node));
        info.putField(findY(node));
        info.putField(findPlane(node));

        Main.getInfo("Region").setField(findRegionTiles(node));
        return info;
    }

    private ClassField findBoundary(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("Boundary")))) {
                return new ClassField("Boundary", f.name, f.desc);
            }
        }
        return new ClassField("Boundary");
    }

    private ClassField findSceneTile(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", node.name))) {
                return new ClassField("SceneTile", f.name, f.desc);
            }
        }
        return new ClassField("SceneTile");
    }

    private ClassField findInteractables(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[L%s;", Main.get("Interactable")))) {
                return new ClassField("Interactables", f.name, f.desc);
            }
        }
        return new ClassField("Interactables");
    }

    private ClassField findWallDecoration(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("WallDecoration")))) {
                return new ClassField("WallDecoration", f.name, f.desc);
            }
        }
        return new ClassField("WallDecoration");
    }

    private ClassField findGroundDecoration(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("GroundDecoration")))) {
                return new ClassField("GroundDecoration", f.name, f.desc);
            }
        }
        return new ClassField("GroundDecoration");
    }

    private ClassField findX(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 1)).var == 2) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 4);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("SceneX", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("SceneX");
    }

    private ClassField findY(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((VarInsnNode)m.instructions.get(i + 1)).var == 3) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 4);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("SceneY", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("SceneY");
    }

    private ClassField findPlane(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.PUTFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                if(i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 3);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("Plane", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("Plane");
    }

    private ClassField findRegionTiles(ClassNode node) {
        for (FieldNode f : Main.getClassNode("Region").fields) {
            if (f.desc.equals(String.format("[[[L%s;", node.name)) && !hasAccess(f, Opcodes.ACC_STATIC)) {
                return new ClassField("Tiles", f.name, f.desc);
            }
        }
        return new ClassField("Tiles");
    }
}
