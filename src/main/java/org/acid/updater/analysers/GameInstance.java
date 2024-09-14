package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

public class GameInstance extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Node"))) {
                continue;
            }

            int int_array_fields = 0;
            int int_3d_array_fields = 0;
            int byte_3d_array_fields = 0;

            for (FieldNode f : n.fields) {
                // [[[NodeDequeue; = GroundItems

                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[I")) {
                    ++int_array_fields;
                }

                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[[[I")) {
                    ++int_3d_array_fields;
                }

                if (!hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals("[[[B")) {
                    ++byte_3d_array_fields;
                }
            }

            if (int_array_fields == 2 && int_3d_array_fields == 2 && byte_3d_array_fields == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("GameInstance", node.name);
        info.putField(findPlayerCount(node));
        info.putField(findPlayers(node));
        info.putField(findPlayerIndices(node));
        info.putField(findNPCCount(node));
        info.putField(findNPCs(node));
        info.putField(findNPCIndices(node));
        info.putField(findField(node, "TileHeights", "getTileHeights"));
        info.putField(findField(node, "TileSettings", "getTileSettings"));
        info.putField(findRegion(node));
        info.putField(findField(node, "Plane", "getPlane"));
        info.putField(findField(node, "BaseX", "getBaseX"));
        info.putField(findField(node, "BaseY", "getBaseY"));
        info.putField(findCollisionMaps(node));
        info.putField(findGroundItems(node));
        info.putField(findField(node, "Projectiles", "getProjectiles"));
        info.putField(findField(node, "IsRegionInstanced", "isInstance"));
        info.putField(findField(node, "RegionInstances", "getInstanceTemplateChunks"));
        info.putField(findField(node, "GraphicsObjects", "getGraphicsObjects"));
        return info;
    }

    private ClassField findPlayerCount(ClassNode node) {
        String npcCountName = "";
        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ICONST_0, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i)).var == 0) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        if (f.desc.equals("I") && !f.name.equals(npcCountName)) {
                            if (npcCountName.isEmpty()) {
                                npcCountName = f.name;
                                continue;
                            }
                            return new ClassField("PlayerCount", f.name, f.desc);
                        }
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("PlayerCount");
    }

    private ClassField findPlayers(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[L%s;", Main.get("Player")))) {
                return new ClassField("Players", f.name, f.desc);
            }
        }
        return new ClassField("Players");
    }

    private ClassField findPlayerIndices(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.SIPUSH, Opcodes.NEWARRAY, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    int length = ((IntInsnNode) m.instructions.get(i + 1)).operand;
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                    if (length == 2048 && f.desc.equals("[I")) {
                        return new ClassField("PlayerIndices", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("PlayerIndices");
    }

    private ClassField findNPCCount(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ICONST_0, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i)).var == 0) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        if (f.desc.equals("I")) {
                            return new ClassField("NPCCount", f.name, f.desc);
                        }
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("NPCCount");
    }

    private ClassField findNPCs(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[L%s;", Main.get("NPC")))) {
                return new ClassField("NPCs", f.name, f.desc);
            }
        }
        return new ClassField("NPCs");
    }

    private ClassField findNPCIndices(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.LDC, Opcodes.NEWARRAY, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    int length = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                    if (length == 65536 && f.desc.equals("[I")) {
                        return new ClassField("NPCIndices", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("NPCIndices");
    }

    private ClassField findTileHeights(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[[[I")) {
                return new ClassField("TileHeights", f.name, f.desc);
            }
        }
        return new ClassField("TileHeights");
    }

    private ClassField findTileSettings(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[[[B")) {
                return new ClassField("TileSettings", f.name, f.desc);
            }
        }
        return new ClassField("TileSettings");
    }

    private ClassField findRegion(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("Scene")))) {
                return new ClassField("Scene", f.name, f.desc);
            }
        }
        return new ClassField("Scene");
    }

    private ClassField findPlane(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD};

        for (MethodNode m : Main.getClass("client").methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(II)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    if (f.desc.equals(String.format("L%s;", node.name))) {
                        FieldInsnNode ff = (FieldInsnNode) m.instructions.get(i + 1);
                        if (ff.desc.equals("I")) {
                            int multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("Plane", ff.owner, ff.name, ff.desc, multi);
                        }
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Plane");
    }

    private ClassField findBaseX(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIIIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i)).var == 11 && ((VarInsnNode) m.instructions.get(i + 4)).var == 12) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            if (f.owner.equals(node.name) && f.desc.equals("I")) {
                                int multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                                return new ClassField("BaseX", f.owner, f.name, f.desc, multi);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("BaseX");
    }

    private ClassField findBaseY(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIIIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i)).var == 11 && ((VarInsnNode) m.instructions.get(i + 4)).var == 13) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            if (f.owner.equals(node.name) && f.desc.equals("I")) {
                                int multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                                return new ClassField("BaseY", f.owner, f.name, f.desc, multi);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("BaseY");
    }

    private ClassField findCollisionMaps(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[L%s;", Main.get("CollisionMap")))) {
                return new ClassField("CollisionMaps", f.name, f.desc);
            }
        }
        return new ClassField("CollisionMaps");
    }

    private ClassField findGroundItems(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[[[L%s;", Main.get("NodeDeque")))) {
                return new ClassField("GroundItems", f.name, f.desc);
            }
        }
        return new ClassField("GroundItems");
    }

    private ClassField findProjectiles(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.INVOKEVIRTUAL, Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL};

        ClassNode client = Main.getClass("client");
        if (client != null) {
            for (MethodNode m : client.methods) {
                if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;)Z", Main.get("PacketWriter")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        MethodInsnNode mm = (MethodInsnNode) m.instructions.get(i);
                        if (mm.desc.equals("(IIII)V") && mm.owner.equals(Main.get("Projectile"))) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            if (f.desc.equals(String.format("L%s;", node.name))) {
                                FieldInsnNode ff = (FieldInsnNode) m.instructions.get(i + 2);
                                return new ClassField("Projectiles", ff.owner, ff.name, ff.desc);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("Projectiles");
    }

    private ClassField findGraphicsObjects(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.INVOKESPECIAL, Opcodes.ASTORE, Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL};

        ClassNode client = Main.getClass("client");
        if (client != null) {
            for (MethodNode m : client.methods) {
                if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;)Z", Main.get("PacketWriter")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        MethodInsnNode mm = (MethodInsnNode) m.instructions.get(i);
                        if (mm.name.equals("<init>") && mm.desc.equals("(IIIIIII)V") && mm.owner.equals(Main.get("GraphicsObject"))) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                            if (f.desc.equals(String.format("L%s;", node.name))) {
                                FieldInsnNode ff = (FieldInsnNode) m.instructions.get(i + 3);
                                return new ClassField("GraphicsObjects", ff.owner, ff.name, ff.desc);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("GraphicsObjects");
    }

    private ClassField findField(ClassNode node, String fieldName, String methodName) {
        for (MethodNode m : node.methods) {
            if (m.name.equals(methodName)) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.GETFIELD});
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField(fieldName, f.name, f.desc, multi);
                }
            }
        }
        return new ClassField(fieldName);
    }
}
