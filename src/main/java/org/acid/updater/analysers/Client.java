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

/**
 * Created by Brandon on 2014-12-07.
 */
public class Client extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (n.name.equals("client")) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Client", node);
        info.putField(findVersion(node));
        info.putField(findClient(node));
        info.putField(findGameInstance(node));
        info.putField(findPlayerUpdateManager(node));
        info.putField(findLocalPlayer(node));
        info.putField(findPlayerIndex(node));
        info.putField(findGameCycle(node));
        info.putField(findGameState(node));
        info.putField(findLoginState(node));
        info.putField(findIsLoading(node));
        info.putField(findCrosshairColour(node));
        info.putField(findAnimationFrameCache(node));
        info.putField(findGrandExchangeOffers(node));
        info.putField(findCameraVertex(node, "X", 1));
        info.putField(findCameraVertex(node, "Y", 2));
        info.putField(findCameraVertex(node, "Z", 7));
        info.putField(findCameraRotation(node, "Pitch", 8, 9));
        info.putField(findCameraRotation(node, "Yaw", 10, 11));
        info.putField(findIsRegionInstanced(node));
        info.putField(findRegionInstances(node));
        info.putField(findDestinationX(node));
        info.putField(findDestinationY(node));
        info.putField(findSineTable(node));
        info.putField(findCosineTable(node));
        info.putField(findItemNodeCache(node));
        info.putField(findWidgets(node));
        info.putField(findWidgetHolder(node));
        info.putField(findGameSettings(node));
        info.putField(findWidgetNodeCache(node));
        info.putField(findWidgetPositionX(node));
        info.putField(findWidgetPositionY(node));
        info.putField(findWidgetWidths(node));
        info.putField(findWidgetHeights(node));
        info.putField(findValidWidgets(node));
        info.putField(findRootInterface(node));
        info.putField(findViewportOffsetX(node));
        info.putField(findViewportOffsetY(node));
        info.putField(findViewPortWidth(node));
        info.putField(findViewPortHeight(node));
        info.putField(findViewPortScale(node));
        info.putField(findMapAngle(node));
        info.putField(findMenu(node));
        info.putField(findIsMenuOpen(node));
        info.putField(findIsResizable(node));
        info.putField(findCurrentLevels(node));
        info.putField(findRealLevels(node));
        info.putField(findExperiences(node));
        info.putField(findCurrentWorld(node));
        info.putField(findEnergy(node));
        info.putField(findWeight(node));
        info.putField(findIsItemSelected(node));
        info.putField(findIsSpellSelected(node));
        return info;
    }

    private ClassField findVersion(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("init") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.SIPUSH, Opcodes.SIPUSH, Opcodes.SIPUSH, Finder.CONSTANT, Finder.OPTIONAL, Opcodes.INVOKEVIRTUAL}); //Optional added: June 15, 2022.
                if (i != -1) {
                    AbstractInsnNode insnNode = m.instructions.get(i + 2);
                    if (insnNode instanceof IntInsnNode revision) {
                        return new ClassField("Revision", String.valueOf(revision.operand), "I");
                    }
                }

                i = new Finder(m).findPattern(new int[]{Opcodes.SIPUSH, Opcodes.BIPUSH});
                if (i != -1) {
                    AbstractInsnNode insnNode = m.instructions.get(i + 1);
                    if (insnNode instanceof IntInsnNode revision) {
                        return new ClassField("Revision", String.valueOf(revision.operand), "I");
                    }
                }
            }
        }

        return new ClassField("Revision");
    }

    private ClassField findClient(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (FieldNode f : n.fields) {
                if (hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("L%s;", node.name))) {
                    return new ClassField("Client", n.name, f.name, f.desc);
                }
            }
        }
        return new ClassField("Client");
    }

    private ClassField findGameInstance(ClassNode node) {
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIIIIIIIIIIIII)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals(String.format("L%s;", Main.get("GameInstance")))) {
                            return new ClassField("GameInstance", f.owner, f.name, f.desc);
                        }
                    }
                }
            }
        }
        return new ClassField("GameInstance");
    }

    private ClassField findPlayerUpdateManager(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (FieldNode fn : n.fields) {
                if (hasAccess(fn, Opcodes.ACC_STATIC)) {
                    if (fn.desc.equals(String.format("L%s;", Main.get("PlayerUpdateManager")))) {
                        return new ClassField("PlayerUpdateManager", n.name, fn.name, fn.desc);
                    }
                }
            }
        }
        return new ClassField("PlayerUpdateManager");
    }

    private ClassField findLocalNPCs(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("[L%s;", Main.get("NPC")))) {
                return new ClassField("LocalNPCs", node.name, f.name, f.desc);
            }
        }

        for (ClassNode n : Main.getClasses()) {
            for (FieldNode f : n.fields) {
                if (hasAccess(f, Opcodes.ACC_STATIC) && f.desc.equals(String.format("[L%s;", Main.get("NPC")))) {
                    return new ClassField("LocalNPCs", node.name, f.name, f.desc);
                }
            }
        }
        return new ClassField("LocalNPCs");
    }

    private ClassField findNpcIndices(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.AALOAD};
        Collection<ClassNode> classes = Main.getClasses();
        for (ClassNode n : classes) {
            for (MethodNode m : n.methods) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    FieldInsnNode g = (FieldInsnNode) m.instructions.get(i + 1);
                    if (g.desc.equals("[I") && f.desc.equals(String.format("[L%s;", Main.get("NPC")))) {
                        return new ClassField("NPCIndices", g.owner, g.name, g.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("NPCIndices");
    }

    private ClassField findNPCCount(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.PUTSTATIC, Opcodes.ICONST_0, Opcodes.PUTSTATIC, Opcodes.ICONST_0, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((FieldInsnNode) m.instructions.get(i)).desc.equals("I") && ((VarInsnNode) m.instructions.get(i + 4)).var == 4) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("NPCCount", f.owner, f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("NPCCount");
    }

    private ClassField findLocalPlayers(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[L%s;", Main.get("Player")))) {
                return new ClassField("LocalPlayers", node.name, f.name, f.desc);
            }
        }

        for (ClassNode n : Main.getClasses()) {
            for (FieldNode f : n.fields) {
                if (f.desc.equals(String.format("[L%s;", Main.get("Player")))) {
                    return new ClassField("LocalPlayers", n.name, f.name, f.desc);
                }
            }
        }

        return new ClassField("LocalPlayers");
    }

    private ClassField findPlayerIndices(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ASTORE, Opcodes.ICONST_0, Opcodes.ISTORE};
        Collection<ClassNode> classes = Main.getClasses();
        for (ClassNode n : classes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;III)V", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals("[I")) {
                            return new ClassField("PlayerIndices", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1, false);
                    }
                }
            }
        }

        //November 9th, 2017.
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTSTATIC, 41);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    if (f.desc.equals("[I")) {
                        return new ClassField("PlayerIndices", f.owner, f.name, f.desc);
                    }
                }
            }
        }
        return new ClassField("PlayerIndices");
    }

    private ClassField findPlayerCount(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIII)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals("I") && ((VarInsnNode) m.instructions.get(i + 3)).var == 19) {
                            return new ClassField("PlayerCount", f.owner, f.name, f.desc);
                        }

                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("PlayerCount");
    }

    private ClassField findLocalPlayer(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("([L%s;IIIIIIII)V", Main.get("Widget"))) || m.desc.equals(String.format("(L%s;I)I", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals(String.format("L%s;", Main.get("Player")))) {
                            return new ClassField("LocalPlayer", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("LocalPlayer");
    }

    private ClassField findPlayerIndex(ClassNode node) {
//        final int[] pattern = new int[]{Opcodes.PUTSTATIC, Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.INVOKEVIRTUAL};
//        for (MethodNode m : node.methods) {
//            if (m.desc.equals("()V")) {
//                int i = new Finder(m).findPattern(pattern, 0, true);
//                while (i != -1) {
//                    if (m.instructions.get(i) instanceof FieldInsnNode && m.instructions.get(i + 1) instanceof FieldInsnNode) {
//                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
//                        FieldInsnNode g = (FieldInsnNode) m.instructions.get(i + 1);
//                        if (f.name.equals(g.name) && f.desc.equals(g.desc) && f.owner.equals(g.owner) && f.owner.equals(node.name)) {
//                            long multi = Main.findMultiplier(f.owner, f.name);
//                            return new ClassField("PlayerIndex", f.owner, f.name, f.desc, multi);
//                        }
//                    }
//                    i = new Finder(m).findPattern(pattern, i + 1, true);
//                }
//            }
//        }

        final int[] pattern2 = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.BIPUSH, Opcodes.ISHL};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern2, 0);
                while (i != -1) {
                    if (((IntInsnNode) m.instructions.get(i + 3)).operand == 8) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("PlayerIndex", f.owner, f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern2, i + 1);
                }
            }
        }

        return new ClassField("PlayerIndex");
    }

    private ClassField findGameCycle(ClassNode node) {
        ClassNode n = Main.getClassNode("DynamicObject");
        if (n != null) {
            for (MethodNode m : n.methods) {
                if (m.name.equals("<init>") && m.desc.equals(String.format("(L%s;IIIIIIIZL%s;)V", Main.get("GameInstance"), Main.get("Renderable")))) {
                    for (AbstractInsnNode a : m.instructions.toArray()) {
                        if (a instanceof FieldInsnNode f) {
                            if (f.owner.equals(node.name)) {
                                long multi = Main.findMultiplier(f.owner, f.name);
                                return new ClassField("GameCycle", f.owner, f.name, f.desc, multi);
                            }
                        }
                    }
                }
            }
        }
        return new ClassField("GameCycle");
    }


    private ClassField findGameState(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_5, Opcodes.IF_ICMPNE};
        for (MethodNode m : node.methods) {
            if (hasAccess(m, Opcodes.ACC_PROTECTED)) { /* && hasAccess(m, Opcodes.ACC_FINAL)*/
                if (m.desc.equals("(Z)V") || m.desc.equals("(I)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;
                        return new ClassField("GameState", f.owner, f.name, f.desc, multi);
                    }
                }
            }
        }
        return new ClassField("GameState");
    }

    private ClassField findLoginState(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ASTORE, Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.ASTORE, Opcodes.GETSTATIC};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (m.instructions.get(i + 4) instanceof FieldInsnNode f) {
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("LoginState", f.owner, f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("LoginState");
    }

    private ClassField findIsLoading(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ICONST_0, Opcodes.INVOKEVIRTUAL, Finder.COMPARISON2, Opcodes.ICONST_0, Opcodes.PUTSTATIC};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIII)V")) {
                    List<AbstractInsnNode> insns = new DeprecatedFinder(m).findPatternInstructions(pattern, 0, false);
                    if (insns != null) {
                        FieldInsnNode f = (FieldInsnNode) insns.get(4);
                        if (f.owner.equals(node.name) && f.desc.equals("Z")) {
                            return new ClassField("IsLoading", f.name, f.desc);
                        }
                    }
                }
            }
        }
        return new ClassField("IsLoading");
    }

    private ClassField findCrosshairColour(ClassNode node) {
        final int[] pattern = new int[]{
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IADD, Opcodes.PUTSTATIC, Opcodes.GETSTATIC,
                Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_0, Finder.COMPARISON
        };

        for (MethodNode m : node.methods) {
            if (m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                    if (f.desc.equals("I")) {
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("CrosshairColour", f.owner, f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("CrosshairColour");
    }

    private ClassField findAnimationFrameCache(ClassNode node) {
        String cache = Main.get("Cache");
        String animationFrames = Main.get("AnimationFrames");
        Collection<ClassNode> nodes = Main.getClasses();
        int[] pattern = {Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.I2L, Opcodes.INVOKEVIRTUAL};

        if (animationFrames != null) {
            for (ClassNode n : nodes) {
                for (MethodNode m : n.methods) {
                    if (m.desc.equals(String.format("(I)L%s;", animationFrames))) {
                        int i = new Finder(m).findPattern(pattern);
                        while (i != -1) {
                            if (((FieldInsnNode) m.instructions.get(i)).desc.equals(String.format("L%s;", cache))) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                                return new ClassField("AnimationFrameCache", f.owner, f.name, f.desc);
                            }
                            i = new Finder(m).findPattern(pattern, i + 1);
                        }
                    }
                }
            }
        }
        return new ClassField("AnimationFrameCache");
    }

    private ClassField findGroundItems(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.contains("[[[L")) {
                return new ClassField("GroundItems", node.name, f.name, f.desc);
            }
        }
        return new ClassField("GroundItems");
    }

    private ClassField findCollisionMap(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[L%s;", Main.get("CollisionMap")))) {
                return new ClassField("CollisionMaps", node.name, f.name, f.desc);
            }
        }
        return new ClassField("CollisionMaps");
    }

    private ClassField findGrandExchangeOffers(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("[L%s;", Main.get("GrandExchangeOffer")))) {
                return new ClassField("GrandExchangeOffers", node.name, f.name, f.desc);
            }
        }
        return new ClassField("GrandExchangeOffers");
    }

    private ClassField findCameraVertex(ClassNode node, String vertex, int varIndex) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD, Opcodes.ISUB, Opcodes.ISTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;III)V", Main.get("GameInstance"))) || m.desc.equals(String.format("(L%s;IIIII)V", Main.get("GameInstance")))) {
                    boolean found = false;
                    for (AbstractInsnNode insnNode : m.instructions) {
                        if (insnNode instanceof IntInsnNode && ((IntInsnNode) insnNode).operand == 13056) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        continue;
                    }

                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 3)).var == varIndex && ((VarInsnNode) m.instructions.get(i + 5)).var == varIndex) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            return new ClassField("Camera" + vertex, f.owner, f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("Camera" + vertex);
    }

    private ClassField findCameraRotation(ClassNode node, String field, int varIndexOne, int varIndexTwo) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.IALOAD, Opcodes.ISTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;III)V", Main.get("GameInstance"))) || m.desc.equals(String.format("(L%s;IIIII)V", Main.get("GameInstance")))) {
                    boolean found = false;
                    for (AbstractInsnNode insnNode : m.instructions) {
                        if (insnNode instanceof IntInsnNode && ((IntInsnNode) insnNode).operand == 13056) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        continue;
                    }

                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 5)).var == varIndexOne || ((VarInsnNode) m.instructions.get(i + 5)).var == varIndexTwo) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            return new ClassField("Camera" + field, f.owner, f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("Camera" + field);
    }

    private ClassField findRegion(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIIIII)V")) {
                    for (AbstractInsnNode a : m.instructions.toArray()) {
                        if (a instanceof FieldInsnNode f) {
                            if (f.desc.equals(String.format("L%s;", Main.get("Scene")))) {
                                return new ClassField("Scene", f.owner, f.name, f.desc);
                            }
                        }
                    }
                }
            }
        }
        return new ClassField("Scene");
    }

    private ClassField findIsRegionInstanced(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.PUTSTATIC};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(ZL%s;)V", Main.get("PacketBuffer")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (f.desc.equals("Z") && f.owner.equals(node.name)) {
                            return new ClassField("IsRegionInstanced", f.name, f.desc);
                        }

                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        return new ClassField("IsRegionInstanced");
    }

    private ClassField findRegionInstances(ClassNode node) {
        //Field 59 <clinit>
        //Only field in client with desc `[[[I`
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.AALOAD, Opcodes.ILOAD, Opcodes.AALOAD, Opcodes.ILOAD, Opcodes.IALOAD};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    if (f.desc.equals("[[[I")) {
                        return new ClassField("RegionInstances", f.name, f.desc);
                    }

                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }

        for (MethodNode m : node.methods) {
            if (m.desc.equals("<clinit>")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTSTATIC, 59);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    if (f.desc.equals("[[[I")) {
                        return new ClassField("RegionInstances", f.name, f.desc);
                    }
                }
            }
        }

        for (FieldNode f : node.fields) {
            if (f.desc.equals("[[[I")) {
                return new ClassField("RegionInstances", f.name, f.desc);
            }
        }
        return new ClassField("RegionInstances");
    }

    private ClassField findPlane(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD, Opcodes.ILOAD};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        FieldInsnNode g = ((FieldInsnNode) m.instructions.get(i + 1));
                        if (f.desc.equals(String.format("L%s;", Main.get("Scene"))) && g.desc.equals("I")) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("Plane", g.owner, g.name, g.desc, multi);
                        }
                    }
                }
            }
        }
        return new ClassField("Plane");
    }

    private ClassField findBaseX(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD, Opcodes.IADD};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 4)).var == 0) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("BaseX", f.owner, f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        //November 9th, 2017.
        final int[] pattern2 = new int[]{Opcodes.GETFIELD, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD, Opcodes.IADD};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern2);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 4)).var == 0) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("BaseX", f.owner, f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern2, i + 1);
                    }
                }
            }
        }
        return new ClassField("BaseX");
    }

    private ClassField findBaseY(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD, Opcodes.IADD};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 4)).var == 1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("BaseY", f.owner, f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        //November 9th, 2017.
        final int[] pattern2 = new int[]{Opcodes.GETFIELD, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD, Opcodes.IADD};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern2);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 4)).var == 1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("BaseY", f.owner, f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern2, i + 1);
                    }
                }
            }
        }
        return new ClassField("BaseY");
    }

    private ClassField findDestinationX(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, Opcodes.GETSTATIC};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (m.instructions.get(i + 7) instanceof FieldInsnNode) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("DestinationX", f.owner, f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        pattern = new int[]{Opcodes.PUTSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.SIPUSH};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.endsWith(")Z")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    if (m.instructions.get(i + 1) instanceof FieldInsnNode ff) {
                        if (f.name.equals(ff.name) && f.owner.equals(ff.owner)) {
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("DestinationX", f.owner, f.name, f.desc, multi);
                        }
                    }

                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("DestinationX");
    }

    private ClassField findDestinationY(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, Opcodes.GETSTATIC};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (m.instructions.get(i + 7) instanceof FieldInsnNode f) {
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("DestinationY", f.owner, f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        String destXName = "";
        pattern = new int[]{Opcodes.PUTSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.SIPUSH};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.endsWith(")Z")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    if (m.instructions.get(i + 1) instanceof FieldInsnNode ff) {
                        if (f.name.equals(ff.name) && f.owner.equals(ff.owner) && !f.name.equals(destXName)) {
                            if (destXName.isEmpty()) {
                                destXName = f.name;
                                continue;
                            }
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("DestinationY", f.owner, f.name, f.desc, multi);
                        }
                    }

                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("DestinationY");
    }

    private ClassField findSineTable(ClassNode node) {
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ISTORE};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;II)V", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 3)).var == 8) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            return new ClassField("SineTable", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.LDC, Opcodes.ILOAD, Opcodes.I2D, Opcodes.LDC, Opcodes.DMUL, Opcodes.INVOKESTATIC};

        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.name.equals("<clinit>")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        MethodInsnNode method = (MethodInsnNode) m.instructions.get(i + pattern.length - 1);
                        if (method.owner.equals("java/lang/Math") && method.name.equals("sin")) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            return new ClassField("SineTable", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("SineTable");
    }

    private ClassField findCosineTable(ClassNode node) {
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ISTORE};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;II)V", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 3)).var == 9) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            return new ClassField("CosineTable", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.LDC, Opcodes.ILOAD, Opcodes.I2D, Opcodes.LDC, Opcodes.DMUL, Opcodes.INVOKESTATIC};

        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.name.equals("<clinit>")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        MethodInsnNode method = (MethodInsnNode) m.instructions.get(i + pattern.length - 1);
                        if (method.owner.equals("java/lang/Math") && method.name.equals("cos")) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            return new ClassField("CosineTable", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("CosineTable");
    }

    private ClassField findTileHeights(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.AALOAD, Opcodes.ASTORE};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(IIIIIIL%s;L%s;)V", Main.get("Scene"), Main.get("CollisionMap")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((FieldInsnNode) m.instructions.get(i)).desc.equals("[[[I") && ((VarInsnNode) m.instructions.get(i + 3)).var == 16) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            return new ClassField("TileHeights", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("TileHeights");
    }

    private ClassField findTileSettings(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.AALOAD, Opcodes.ILOAD};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(IIIIIIL%s;L%s;)V", Main.get("Scene"), Main.get("CollisionMap")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((FieldInsnNode) m.instructions.get(i)).desc.equals("[[[B") && ((VarInsnNode) m.instructions.get(i + 1)).var == 0 && ((VarInsnNode) m.instructions.get(i + 3)).var == 1) {
                            //Added new Finder(m) on August 21st, 2019.
                            int j = new Finder(m).findNext(i + pattern.length, Opcodes.IAND);
                            if (j != -1 && j < i + pattern.length + 5) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                                return new ClassField("TileSettings", f.owner, f.name, f.desc);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("TileSettings");
    }

    private ClassField findItemNodeCache(ClassNode node) {
        ClassInfo info = Main.getInfo("ItemNode");
        ClassField field = info.getField("Cache");
        return new ClassField("ItemNodeCache", info.getName(), field.getName(), field.getDesc());
    }

    private ClassField findWidgets(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.AALOAD};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(I)Z")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals(String.format("[[L%s;", Main.get("Widget")))) {
                            return new ClassField("Widgets", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        ClassInfo info = Main.getInfo("WidgetHolder");
        if (info != null) {
            ClassField widgets = info.getField("Widgets");
            return new ClassField("Widgets", info.getName(), widgets.getName(), widgets.getDesc());
        }
        return new ClassField("Widgets");
    }

    private ClassField findWidgetHolder(ClassNode node) {
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.ILOAD};
        for (MethodNode m : node.methods) {
            int i = new Finder(m).findPattern(pattern);
            while (i != -1) {
                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                FieldInsnNode g = (FieldInsnNode) m.instructions.get(i + 1);
                if (f.desc.equals(String.format("L%s;", Main.get("WidgetHolder"))) && g.desc.equals(String.format("[[L%s;", Main.get("Widget")))) {
                    return new ClassField("WidgetHolder", f.owner, f.name, f.desc);
                }
                i = new Finder(m).findPattern(pattern, i + 1);
            }
        }
        return new ClassField("WidgetHolder");
    }

    private ClassField findGameSettings(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.IALOAD, Opcodes.ISTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("()Z")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals("[I") && ((VarInsnNode) m.instructions.get(i + 6)).var == 2) {
                            return new ClassField("GameSettings", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }


        //Alternative: August 6th, 2017.
        pattern = new int[]{Opcodes.ISTORE, Opcodes.GETSTATIC, Opcodes.ILOAD};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(IIIILjava/lang/String;Ljava/lang/String;II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i)).var == ((VarInsnNode) m.instructions.get(i + 2)).var) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            return new ClassField("GameSettings", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        return new ClassField("GameSettings");
    }

    private ClassField findWidgetNodeCache(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.I2L};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("([L%s;IIIIIIII)V", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 1)).var == 11) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            return new ClassField("WidgetNodeCache", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("WidgetNodeCache");
    }

    private ClassField findWidgetPositionX(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("([L%s;IIIIIIII)V", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {

                        int j = new Finder(m).findNext(i + pattern.length, Opcodes.ILOAD);

                        if (j != -1) {
                            if (((VarInsnNode) m.instructions.get(j)).var == 6) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                                return new ClassField("WidgetPositionsX", f.owner, f.name, f.desc);
                            }
                        }

                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        return new ClassField("WidgetPositionsX");
    }

    private ClassField findWidgetPositionY(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("([L%s;IIIIIIII)V", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {

                        int j = new Finder(m).findNext(i + pattern.length, Opcodes.ILOAD);

                        if (j != -1) {
                            if (((VarInsnNode) m.instructions.get(j)).var == 7) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                                return new ClassField("WidgetPositionsY", f.owner, f.name, f.desc);
                            }
                        }

                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        return new ClassField("WidgetPositionsY");
    }

    private ClassField findWidgetWidths(ClassNode node) {
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ALOAD, Opcodes.GETFIELD};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("([L%s;IIIIIIII)V", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode widget_field = (FieldInsnNode) m.instructions.get(i + pattern.length - 1);

                        if (widget_field.name.equals(Main.getInfo("Widget").getField("Width").getName())) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            return new ClassField("WidgetWidths", f.owner, f.name, f.desc);
                        }

                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        pattern = new int[]{Opcodes.BIPUSH, Opcodes.NEWARRAY, Opcodes.PUTSTATIC};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<clinit>")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((IntInsnNode) m.instructions.get(i)).operand == 100 && ((FieldInsnNode) m.instructions.get(i + 2)).desc.equals("[I")) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                        return new ClassField("WidgetWidths", f.owner, f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }

        return new ClassField("WidgetWidths");
    }

    private ClassField findWidgetHeights(ClassNode node) {
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ALOAD, Opcodes.GETFIELD};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("([L%s;IIIIIIII)V", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode widget_field = (FieldInsnNode) m.instructions.get(i + pattern.length - 1);

                        if (widget_field.name.equals(Main.getInfo("Widget").getField("Height").getName())) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            return new ClassField("WidgetHeights", f.owner, f.name, f.desc);
                        }

                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        return new ClassField("WidgetHeights");
    }

    private ClassField findValidWidgets(ClassNode node) {
        ClassNode widgetHolder = Main.getClassNode("WidgetHolder");
        if (widgetHolder != null) {
            int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.ICONST_0, Opcodes.BASTORE};
            for (MethodNode m : widgetHolder.methods) {
                if (m.desc.equals("(I)V") && !hasAccess(m, Opcodes.ACC_STATIC)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (((VarInsnNode) m.instructions.get(i + 1)).var == 1 && f.desc.equals("[Z")) {
                            return new ClassField("ValidWidgets", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        Collection<ClassNode> nodes = Main.getClasses();
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.BALOAD, Finder.COMPARISON2};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(I)V") && !hasAccess(m, Opcodes.ACC_STATIC)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (((VarInsnNode) m.instructions.get(i + 1)).var == 0 && f.desc.equals("[Z")) {
                            return new ClassField("ValidWidgets", f.owner, f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("ValidWidgets");
    }

    private ClassField findRootInterface(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_1, Opcodes.INVOKESTATIC};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(III)L%s;", Main.get("WidgetNode")))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((FieldInsnNode) m.instructions.get(i)).desc.equals("I")) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("WidgetRootInterface", f.owner, f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("WidgetRootInterface");
    }

    private ClassField findViewportOffsetX(ClassNode node) {
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD, Opcodes.ISUB, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(II)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    VarInsnNode v = (VarInsnNode) m.instructions.get(i + 3);
                    if (v.var == 1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals("I")) {
                            return new ClassField("ViewportOffsetX", f.owner, f.name, f.desc);
                        }
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("ViewportOffsetX");
    }

    private ClassField findViewportOffsetY(ClassNode node) {
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD, Opcodes.ISUB, Opcodes.ISTORE};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(II)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    VarInsnNode v = (VarInsnNode) m.instructions.get(i + 3);
                    if (v.var == 2) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals("I")) {
                            return new ClassField("ViewportOffsetY", f.owner, f.name, f.desc);
                        }
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("ViewportOffsetY");
    }

    private ClassField findViewPortWidth(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_2, Opcodes.IDIV,
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;III)V", Main.get("GameInstance"))) || m.desc.equals(String.format("(L%s;IIIII)V", Main.get("GameInstance")))) {
                    boolean found = false;
                    for (AbstractInsnNode insnNode : m.instructions) {
                        if (insnNode instanceof IntInsnNode && ((IntInsnNode) insnNode).operand == 13056) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        continue;
                    }

                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 8)).var == 1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            if (f.desc.equals("I")) {
                                long multi = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;
                                return new ClassField("ViewPortWidth", f.owner, f.name, f.desc, multi);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("ViewPortWidth");
    }

    private ClassField findViewPortHeight(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_2, Opcodes.IDIV,
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;III)V", Main.get("GameInstance"))) || m.desc.equals(String.format("(L%s;IIIII)V", Main.get("GameInstance")))) {
                    boolean found = false;
                    for (AbstractInsnNode insnNode : m.instructions) {
                        if (insnNode instanceof IntInsnNode && ((IntInsnNode) insnNode).operand == 13056) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        continue;
                    }

                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i + 8)).var == 7) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            if (f.desc.equals("I")) {
                                long multi = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;
                                return new ClassField("ViewPortHeight", f.owner, f.name, f.desc, multi);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("ViewPortHeight");
    }

    private ClassField findViewPortScale(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ICONST_2, Opcodes.IDIV, Opcodes.GETSTATIC, Opcodes.LDC};
        final int[] pattern2 = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ILOAD, Opcodes.IMUL, Opcodes.ILOAD, Opcodes.IDIV};
        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;III)V", Main.get("GameInstance"))) || m.desc.equals(String.format("(L%s;IIIII)V", Main.get("GameInstance")))) {
                    boolean found = false;
                    for (AbstractInsnNode insnNode : m.instructions) {
                        if (insnNode instanceof IntInsnNode && ((IntInsnNode) insnNode).operand == 13056) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        continue;
                    }

                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (m.instructions.get(i + 2) instanceof FieldInsnNode f) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 3)).cst;

                            if (f.desc.equals("I")) {
                                return new ClassField("ViewPortScale", f.owner, f.name, f.desc, multi);
                            }
                        }

                        i = new Finder(m).findPattern(pattern, i + 1);
                    }

                    i = new Finder(m).findPattern(pattern2);
                    while (i != -1) {
                        if (m.instructions.get(i) instanceof FieldInsnNode f) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;

                            if (f.desc.equals("I")) {
                                return new ClassField("ViewPortScale", f.owner, f.name, f.desc, multi);
                            }
                        }

                        i = new Finder(m).findPattern(pattern2, i + 1);
                    }
                }
            }
        }

        return new ClassField("ViewPortScale");
    }

    private ClassField findMapAngle(ClassNode node) {
        //(IIIILvc;Lnw;)V
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.SIPUSH, Opcodes.IAND};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(IIIIL%s;L%s;)V", Main.get("ImageRGB"), Main.get("SpriteMask")))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;
                        return new ClassField("MapAngle", f.owner, f.name, f.desc, multi);
                    }
                }
            }
        }

        return new ClassField("MapAngle");
    }

    private ClassField findMapScale(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.GETSTATIC, Opcodes.LDC};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("([L%s;IIIIIIII)V", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 4)).cst;
                        return new ClassField("MapScale", f.owner, f.name, f.desc, multi);
                    }
                }
            }
        }

        return new ClassField("MapScale");
    }

    private ClassField findMapOffset(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.IADD, Opcodes.SIPUSH, Opcodes.IAND};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("([L%s;IIIIIIII)V", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 3);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 4)).cst;
                        return new ClassField("MapOffset", f.owner, f.name, f.desc, multi);
                    }
                }
            }
        }

        return new ClassField("MapOffset");
    }

    private ClassField findMenu(ClassNode node) {
        String menuName = Main.get("Menu");
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", menuName))) {
                return new ClassField("Menu", f.name, f.desc);
            }
        }

        return new ClassField("Menu");
    }

    private ClassField findMenuCount(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Finder.COMPARISON};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (f.owner.equals("client")) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("MenuCount", f.owner, f.name, f.desc, multi);
                        }
                    }
                }
            }
        }

        return new ClassField("MenuCount");
    }

    private ClassField findMenuActions(ClassNode node) {
        int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.AALOAD, Opcodes.ASTORE};

        for (ClassNode n : new ClassNode[]{node}) {
            for (MethodNode m : n.methods) {
                if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals("[Ljava/lang/String;") && f.owner.equals("client")) {
                            return new ClassField("MenuActions", f.owner, f.name, f.desc);
                        }
                    }
                }
            }
        }

        pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.AALOAD, Opcodes.ARETURN};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(I)Ljava/lang/String;")) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals("[Ljava/lang/String;") && f.owner.equals("client")) {
                            return new ClassField("MenuActions", f.owner, f.name, f.desc);
                        }
                    }
                }
            }
        }

        return new ClassField("MenuActions");
    }

    private ClassField findMenuOptions(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.AALOAD, Opcodes.INVOKEVIRTUAL, Finder.COMPARISON2};

        for (ClassNode n : new ClassNode[]{node}) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals("[Ljava/lang/String;") && f.owner.equals("client")) {
                            return new ClassField("MenuOptions", f.owner, f.name, f.desc);
                        }
                    }
                }
            }
        }

        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(I)Ljava/lang/String;")) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                        if (f.desc.equals("[Ljava/lang/String;") && f.owner.equals("client")) {
                            return new ClassField("MenuOptions", f.owner, f.name, f.desc);
                        }
                    }
                }
            }
        }

        return new ClassField("MenuOptions");
    }

    private ClassField findIsMenuOpen(ClassNode node) {
        int[] pattern = new int[]{Opcodes.ICONST_1, Opcodes.PUTSTATIC};
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(II)V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    if (f.desc.equals("Z")) {
                        return new ClassField("IsMenuOpen", f.owner, f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("IsMenuOpen");
    }

    private ClassField findMenuX(ClassNode node) {
        int[] pattern = new int[]{
                Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, //MenuX
                Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, //MenuY
                Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, //MenuWidth
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.LDC    //MenuHeight
        };

        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(II)V")) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    while (i != -1) {
                        if (m.instructions.get(i) instanceof VarInsnNode) {
                            int j = new Finder(m).findNextInstruction(i, Opcodes.PUTSTATIC, 0);
                            if (j != -1) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                                long multi = Main.findMultiplier(f.owner, f.name);
                                return new ClassField("MenuX", f.owner, f.name, f.desc, multi);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1, false);
                    }
                }
            }
        }

        //May 5th, 2018
        final int[] pattern2 = new int[]{
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu X
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Y
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Width
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE //Menu Height
        };
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern2);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("MenuX", f.owner, f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("MenuX");
    }

    private ClassField findMenuY(ClassNode node) {
        int[] pattern = new int[]{
                Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, //MenuX
                Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, //MenuY
                Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, //MenuWidth
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.LDC    //MenuHeight
        };

        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(II)V")) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    while (i != -1) {
                        if (m.instructions.get(i) instanceof VarInsnNode) {
                            int j = new Finder(m).findNextInstruction(i, Opcodes.PUTSTATIC, 1);
                            if (j != -1) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                                long multi = Main.findMultiplier(f.owner, f.name);
                                return new ClassField("MenuY", f.owner, f.name, f.desc, multi);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1, false);
                    }
                }
            }
        }

        //May 5th, 2018
        final int[] pattern2 = new int[]{
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu X
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Y
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Width
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE //Menu Height
        };
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern2);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 4);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("MenuY", f.owner, f.name, f.desc, multi);
                }
            }
        }

        return new ClassField("MenuY");
    }

    private ClassField findMenuWidth(ClassNode node) {
        int[] pattern = new int[]{
                Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, //MenuX
                Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, //MenuY
                Opcodes.ILOAD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTSTATIC, //MenuWidth
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.LDC    //MenuHeight
        };

        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(II)V")) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    while (i != -1) {
                        if (m.instructions.get(i) instanceof VarInsnNode) {
                            int j = new Finder(m).findNextInstruction(i, Opcodes.PUTSTATIC, 2);
                            if (j != -1) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                                long multi = Main.findMultiplier(f.owner, f.name);
                                return new ClassField("MenuWidth", f.owner, f.name, f.desc, multi);
                            }
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }

        //May 5th, 2018
        final int[] pattern2 = new int[]{
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu X
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Y
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Width
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE //Menu Height
        };
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern2);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 8);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("MenuWidth", f.owner, f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("MenuWidth");
    }

    private ClassField findMenuHeight(ClassNode node) {
        int[] pattern = new int[]{Opcodes.LDC, Opcodes.IMUL, Opcodes.LDC, Opcodes.IADD, Opcodes.PUTSTATIC};

        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("(II)V")) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + pattern.length - 1);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("MenuHeight", f.owner, f.name, f.desc, multi);
                    }
                }
            }
        }

        //May 5th, 2018
        final int[] pattern2 = new int[]{
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu X
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Y
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE, //Menu Width
                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISTORE //Menu Height
        };
        for (MethodNode m : node.methods) {
            if (!hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern2);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 12);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("MenuHeight", f.owner, f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("MenuHeight");
    }

    private ClassField findIsResizable(ClassNode node) {
        final int[] pattern = new int[]{
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETSTATIC, Opcodes.LDC,
                Opcodes.INVOKEVIRTUAL,
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETSTATIC, Finder.COMPARISON2
        };

        for (MethodNode m : node.methods) {
            if (m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 7);
                    if (f.desc.equals("Z") && f.owner.equals(node.name)) {
                        return new ClassField("IsResizable", f.owner, f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("IsResizable");
    }

    private ClassField findCurrentLevels(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ICONST_1, Finder.COMPARISON};

        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;I)I", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    if (i != -1) {
                        int j = new Finder(m).findNext(i, Opcodes.GETSTATIC, false);
                        if (j != -1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                            return new ClassField("CurrentLevels", f.owner, f.name, f.desc);
                        }
                    }
                }
            }
        }
        return new ClassField("CurrentLevels");
    }

    private ClassField findRealLevels(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ICONST_2, Finder.COMPARISON};

        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;I)I", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    if (i != -1) {
                        int j = new Finder(m).findNext(i, Opcodes.GETSTATIC, false);
                        if (j != -1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                            return new ClassField("RealLevels", f.owner, f.name, f.desc);
                        }
                    }
                }
            }
        }
        return new ClassField("RealLevels");
    }

    private ClassField findExperiences(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ICONST_3, Finder.COMPARISON};

        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;I)I", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    if (i != -1) {
                        int j = new Finder(m).findNext(i, Opcodes.GETSTATIC, false);
                        if (j != -1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                            return new ClassField("Experiences", f.owner, f.name, f.desc);
                        }
                    }
                }
            }
        }
        return new ClassField("Experiences");
    }

    private ClassField findCurrentWorld(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.LDC, Opcodes.IADD};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals("()V")) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if ((int) ((LdcInsnNode) m.instructions.get(i + 3)).cst == 40000 || (int) ((LdcInsnNode) m.instructions.get(i + 3)).cst == 50000) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 1)).cst;
                            return new ClassField("CurrentWorld", f.owner, f.name, f.desc, multi);
                        }

                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("CurrentWorld");
    }

    private ClassField findEnergy(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.BIPUSH, Finder.COMPARISON, Finder.OPTIONAL, Opcodes.GETSTATIC, Opcodes.LDC};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;I)I", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    while (i != -1) {
                        if (((IntInsnNode) m.instructions.get(i + 1)).operand == 11) {
                            int j = new Finder(m).findNext(i, Opcodes.GETSTATIC, false);

                            if (j != -1) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                                long multi = (int) ((LdcInsnNode) m.instructions.get(j + 1)).cst;
                                return new ClassField("EnergyLevel", f.owner, f.name, f.desc, multi);
                            }
                        }

                        i = new Finder(m).findPattern(pattern, i + 1, false);
                    }
                }
            }
        }
        return new ClassField("EnergyLevel");
    }

    private ClassField findWeight(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.BIPUSH, Finder.COMPARISON, Finder.OPTIONAL, Opcodes.GETSTATIC, Opcodes.LDC};

        Collection<ClassNode> nodes = Main.getClasses();
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;I)I", Main.get("Widget")))) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    while (i != -1) {
                        if (((IntInsnNode) m.instructions.get(i + 1)).operand == 12) {
                            int j = new Finder(m).findNext(i, Opcodes.GETSTATIC, false);

                            if (j != -1) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(j);
                                long multi = (int) ((LdcInsnNode) m.instructions.get(j + 1)).cst;
                                return new ClassField("PlayerWeight", f.owner, f.name, f.desc, multi);
                            }
                        }

                        i = new Finder(m).findPattern(pattern, i + 1, false);
                    }
                }
            }
        }
        return new ClassField("PlayerWeight");
    }

    private ClassField findIsItemSelected(ClassNode node) {
        var pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.IFNE, Opcodes.GETSTATIC, Opcodes.IFNE, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.BIPUSH};
        var classes = Main.getClasses();
        for (var c : classes) {
            for (var m : c.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIII)V")) {
                    for (int i = 0; i < m.instructions.size(); i++) {
                        var instructions = new DeprecatedFinder(m).findPatternInstructions(pattern, i, false);
                        if (instructions != null) {
                            FieldInsnNode f = (FieldInsnNode) instructions.getFirst();
                            if (hasAccess(m, Opcodes.ACC_STATIC) && f.desc.equals("I")) {
                                long multi = (int) ((LdcInsnNode) f.getNext()).cst;
                                return new ClassField("IsItemSelected", f.owner, f.name, f.desc, multi);
                            }
                        }
                    }
                }
            }
        }
        return new ClassField("IsItemSelected");
    }

    private ClassField findIsSpellSelected(ClassNode node) {
        var pattern = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.IFNE, Opcodes.GETSTATIC, Opcodes.IFNE, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.BIPUSH};
        var classes = Main.getClasses();
        for (var c : classes) {
            for (var m : c.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals("(IIII)V")) {
                    for (int i = 0; i < m.instructions.size(); i++) {
                        var instructions = new DeprecatedFinder(m).findPatternInstructions(pattern, i, false);
                        if (instructions != null) {
                            FieldInsnNode f = (FieldInsnNode) instructions.get(4);
                            if (hasAccess(m, Opcodes.ACC_STATIC) && f.desc.equals("Z")) {
                                return new ClassField("IsSpellSelected", f.owner, f.name, f.desc);
                            }
                        }
                    }
                }
            }
        }
        return new ClassField("IsSpellSelected");
    }

    /*private ClassField findCharacterCombatCycle(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Finder.WILDCARD, Finder.OPTIONAL, Opcodes.GETSTATIC, Finder.OPTIONAL, Opcodes.IMUL, Opcodes.IF_ICMPLE};
        for (MethodNode m : node.methods) {
            if (m.desc.equals(String.format("(L%s;IIIII)V", Main.get("Actor")))) {
                int i = new Finder(m).findPattern(pattern);

                while(i != -1) {
                    int multi = 0;
                    if (m.instructions.get(i - 1) instanceof LdcInsnNode) {
                        multi = (int) ((LdcInsnNode) m.instructions.get(i - 1)).cst;
                    } else if (m.instructions.get(i + 2) instanceof LdcInsnNode) {
                        multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                    } else if (m.instructions.get(i + 2) instanceof InsnNode) {
                        multi = (int) ((LdcInsnNode) m.instructions.get(i - 3)).cst;
                    }

                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    if (f.desc.equals("I")) {
                        return new ClassField("CombatCycle", f.owner, f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("CombatCycle");
    }*/
}
