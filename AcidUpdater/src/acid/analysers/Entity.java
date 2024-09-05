package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-07.
 */
public class Entity extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Animable")) || !hasAccess(n, Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC)) {
                continue;
            }

            int int_arr_count = 0, str_count = 0;

            for (FieldNode f : n.fields) {
                if (f.desc.equals("[I") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++int_arr_count;
                } else if (f.desc.equals("Ljava/lang/String;") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                    ++str_count;
                }
            }

            if (str_count == 1 && int_arr_count >= 5) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Entity", node.name);
        info.putField(findAnimationID(node));
        info.putField(findAnimationDelay(node));
        info.putField(findAnimationFrame(node));
        info.putField(findMovementSequence(node));
        info.putField(findMovementFrame(node));
        info.putField(findCurrentSequence(node));
        info.putField(findSpokenText(node));
        info.putField(findHitDamages(node));
        info.putField(findHitTypes(node));
        info.putField(findHitCycle(node));
        info.putField(findQueueX(node, null));
        info.putField(findQueueY(node, null));
        info.putField(findQueueTraversed(node));
        info.putField(findQueueLength(node));
        info.putField(findLocalX(node, info.getField("QueueX")));
        info.putField(findLocalY(node, info.getField("QueueY")));
        //info.putField(findCombatCycle(node));
        info.putField(findInteractingIndex(node));
        //info.putField(findRotation(node));
        info.putField(new ClassField("Orientation"));
        info.putField(findIsWalking(node));
        //info.putField(findTargetIndex(node));

        if (Main.get("CombatInfo2") != null) {
            info.putField(findCombatInfoList(node));
            Main.getInfo("CombatInfo2").setField(findCombatInfoHealthScale(node));
        }
        else {
            info.putField(findHealth(node));
            info.putField(findMaxHealth(node));
        }

        info.putField(findSpotAnimation(node));
        info.putField(findSpotAnimationFrame(node));
        info.putField(findSpotAnimationFrameCycle(node));
        //info.putField(findGraphicsId(node));
        return info;
    }

    private ClassField findSpokenText(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("Ljava/lang/String;")) {
                return new ClassField("SpokenText", f.name, f.desc);
            }
        }
        return new ClassField("SpokenText");
    }

    private ClassField findAnimationID(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) { //IIZI - Changed November 8th, 2017.
                int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL});
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    if (f.desc.equals("I")) {
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                        return new ClassField("AnimationID", f.name, f.desc, multi);
                    }
                }
            }
        }

        return new ClassField("AnimationID");
    }

    private ClassField findAnimationDelay(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_0, Opcodes.IF_ICMPNE};
        final int[] pattern2 = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_1, Opcodes.IF_ICMPGT};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;)V", node.name)) && hasAccess(m, Opcodes.ACC_STATIC)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (f.owner.equals(node.name)) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("AnimationDelay", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }

                    i = new Finder(m).findPattern(pattern2);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (f.owner.equals(node.name)) {
                            long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("AnimationDelay", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern2, i + 1);
                    }
                }
            }
        }
        return new ClassField("AnimationDelay");
    }

    private ClassField findAnimationFrame(ClassNode node) {
        return new ClassField("AnimationFrame");
    }

    private ClassField findMovementSequence(ClassNode node) {
        return new ClassField("MovementSequence");
    }

    private ClassField findMovementFrame(ClassNode node) {
        return new ClassField("MovementFrame");
    }

    private ClassField findCurrentSequence(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 2);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("CurrentSequence", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("CurrentSequence");
    }

    private ClassField findHitDamages(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.IASTORE};
        for (MethodNode m : node.methods) {
            if ((m.desc.equals("(IIIB)V") || m.desc.equals("(IIII)V") || m.desc.equals("(IIIIII)V")) && hasAccess(m, Opcodes.ACC_FINAL)) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 3)).var == 2) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        return new ClassField("HitDamages", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("HitDamages");
    }

    private ClassField findHitTypes(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.IASTORE};
        for (MethodNode m : node.methods) {
            if ((m.desc.equals("(IIIB)V") || m.desc.equals("(IIII)V") || m.desc.equals("(IIIIII)V")) && hasAccess(m, Opcodes.ACC_FINAL)) {
                int i = new Finder(m).findPattern(pattern);
                while (i != -1) {
                    if (((VarInsnNode) m.instructions.get(i + 3)).var == 1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        return new ClassField("HitTypes", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("HitTypes");
    }

    private ClassField findHitCycle(ClassNode node) {
        for (MethodNode m : node.methods) {
            if ((m.desc.equals("(IIIB)V") || m.desc.equals("(IIII)V") || m.desc.equals("(IIIIII)V")) && hasAccess(m, Opcodes.ACC_FINAL)) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD});
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    return new ClassField("HitCycle", f.name, f.desc);
                }
            }
        }
        return new ClassField("HitCycle");
    }

    private ClassField findLocalX(ClassNode node, ClassField queueX) {
        if (queueX != null) {
            final int[] pattern = new int[]{Opcodes.IMUL, Opcodes.IADD, Opcodes.PUTFIELD};
            for (MethodNode m : node.methods) {
                if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(pattern);
                    int j = i;
                    while (i != -1 && j != -1) {
                        j = new Finder(m).findPrev(j - 1, Opcodes.GETFIELD);
                        FieldInsnNode q = ((FieldInsnNode) m.instructions.get(j));

                        if (q.desc.equals("[I")) {
                            if (q.name.equals(queueX.getName())) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                                long multi = Main.findMultiplier(f.owner, f.name);
                                return new ClassField("LocalX", f.name, f.desc, multi);
                            } else {
                                i = j = new Finder(m).findPattern(pattern, i + 1);
                            }
                        }
                    }
                }
            }

            //April 29th, 2017.
            for (ClassNode n : Main.getClasses()) {
                if (n.superName.equals(node.name)) {
                    for (MethodNode m : n.methods) {
                        if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                            int i = new Finder(m).findPattern(pattern);
                            int j = i;
                            while (i != -1 && j != -1) {
                                j = new Finder(m).findPrev(j - 1, Opcodes.GETFIELD);
                                FieldInsnNode q = ((FieldInsnNode) m.instructions.get(j));

                                if (q.desc.equals("[I")) {
                                    if (q.name.equals(queueX.getName())) {
                                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                                        long multi = Main.findMultiplier(f.owner, f.name);
                                        return new ClassField("LocalX", f.name, f.desc, multi);
                                    } else {
                                        i = j = new Finder(m).findPattern(pattern, i + 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            //April 29th, 2017.
            final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.INVOKESTATIC};
            for (ClassNode n : Main.getClasses()) {
                for (MethodNode m : n.methods) {
                    if (m.desc.equals(String.format("(L%s;)V", node.name)) && hasAccess(m, Opcodes.ACC_STATIC)) {
                        int i = new Finder(m).findPattern(pattern);
                        if (i != -1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("LocalX", f.name, f.desc, multi);
                        }
                    }
                }
            }
        }
        return new ClassField("LocalX");
    }

    private ClassField findLocalY(ClassNode node, ClassField queueY) {
        if (queueY != null) {
            final int[] pattern = new int[]{Opcodes.IMUL, Opcodes.IADD, Opcodes.PUTFIELD};
            for (MethodNode m : node.methods) {
                if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(pattern);
                    int j = i;
                    while (i != -1 && j != -1) {
                        j = new Finder(m).findPrev(j - 1, Opcodes.GETFIELD);
                        FieldInsnNode q = ((FieldInsnNode) m.instructions.get(j));

                        if (q.desc.equals("[I")) {
                            if (q.name.equals(queueY.getName())) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                                long multi = Main.findMultiplier(f.owner, f.name);
                                return new ClassField("LocalY", f.name, f.desc, multi);
                            } else {
                                i = j = new Finder(m).findPattern(pattern, i + 1);
                            }
                        }
                    }
                }
            }

            //April 29th, 2017.
            for (ClassNode n : Main.getClasses()) {
                if (n.superName.equals(node.name)) {
                    for (MethodNode m : n.methods) {
                        if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                            int i = new Finder(m).findPattern(pattern);
                            int j = i;
                            while (i != -1 && j != -1) {
                                j = new Finder(m).findPrev(j - 1, Opcodes.GETFIELD);
                                FieldInsnNode q = ((FieldInsnNode) m.instructions.get(j));

                                if (q.desc.equals("[I")) {
                                    if (q.name.equals(queueY.getName())) {
                                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                                        long multi = Main.findMultiplier(f.owner, f.name);
                                        return new ClassField("LocalY", f.name, f.desc, multi);
                                    } else {
                                        i = j = new Finder(m).findPattern(pattern, i + 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            //April 29th, 2017.
            final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.INVOKESTATIC};
            for (ClassNode n : Main.getClasses()) {
                for (MethodNode m : n.methods) {
                    if (m.desc.equals(String.format("(L%s;)V", node.name)) && hasAccess(m, Opcodes.ACC_STATIC)) {
                        int i = new Finder(m).findPattern(pattern);
                        if (i != -1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 5);
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("LocalY", f.name, f.desc, multi);
                        }
                    }
                }
            }
        }
        return new ClassField("LocalY");
    }

    private ClassField findQueueX(ClassNode node, ClassField localX) {
        if (localX == null) {
            final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ICONST_0, Opcodes.IALOAD};
            for (MethodNode m : node.methods) {
                if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i)).var == 1) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                            return new ClassField("QueueX", f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }

            //April 29th, 2017.
            for (ClassNode n : Main.getClasses()) {
                if (n.superName.equals(node.name)) {
                    for (MethodNode m : n.methods) {
                        if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                            int i = new Finder(m).findPattern(pattern);
                            while (i != -1) {
                                if (((VarInsnNode) m.instructions.get(i)).var == 1) {
                                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                                    return new ClassField("QueueX", f.name, f.desc);
                                }
                                i = new Finder(m).findPattern(pattern, i + 1);
                            }
                        }
                    }
                }
            }
        }
        else {
            //April 29th, 2017.
            final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ICONST_0, Opcodes.IALOAD, Opcodes.IMUL, Opcodes.IADD, Opcodes.PUTFIELD};
            for (ClassNode n : Main.getClasses()) {
                for (MethodNode m : n.methods) {
                    if (m.desc.equals(String.format("(L%s;)V", node.name)) && hasAccess(m, Opcodes.ACC_STATIC)) {
                        int i = new Finder(m).findPattern(pattern);
                        while (i != -1) {
                            if (((FieldInsnNode) m.instructions.get(i + 6)).name.equals(localX.getName())) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                                long multi = Main.findMultiplier(f.owner, f.name);
                                return new ClassField("QueueX", f.name, f.desc, multi);
                            }
                            i = new Finder(m).findPattern(pattern, i + 1);
                        }
                    }
                }
            }
        }
        return new ClassField("QueueX");
    }

    private ClassField findQueueY(ClassNode node, ClassField localY) {
        if (localY == null) {
            final int[] pattern = new int[]{Opcodes.ILOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ICONST_0, Opcodes.IALOAD};
            for (MethodNode m : node.methods) {
                if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        if (((VarInsnNode) m.instructions.get(i)).var == 2) {
                            FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                            return new ClassField("QueueY", f.name, f.desc);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }

            //April 29th, 2017.
            for (ClassNode n : Main.getClasses()) {
                if (n.superName.equals(node.name)) {
                    for (MethodNode m : n.methods) {
                        if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                            int i = new Finder(m).findPattern(pattern);
                            while (i != -1) {
                                if (((VarInsnNode) m.instructions.get(i)).var == 2) {
                                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 2);
                                    return new ClassField("QueueY", f.name, f.desc);
                                }
                                i = new Finder(m).findPattern(pattern, i + 1);
                            }
                        }
                    }
                }
            }
        }
        else {
            //April 29th, 2017.
            final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ICONST_0, Opcodes.IALOAD, Opcodes.IMUL, Opcodes.IADD, Opcodes.PUTFIELD};
            for (ClassNode n : Main.getClasses()) {
                for (MethodNode m : n.methods) {
                    if (m.desc.equals(String.format("(L%s;)V", node.name)) && hasAccess(m, Opcodes.ACC_STATIC)) {
                        int i = new Finder(m).findPattern(pattern);
                        while (i != -1) {
                            if (((FieldInsnNode) m.instructions.get(i + 6)).name.equals(localY.getName())) {
                                FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                                long multi = Main.findMultiplier(f.owner, f.name);
                                return new ClassField("QueueY", f.name, f.desc, multi);
                            }
                            i = new Finder(m).findPattern(pattern, i + 1);
                        }
                    }
                }
            }
        }
        return new ClassField("QueueY");
    }

    private ClassField findQueueTraversed(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                int i = new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ICONST_0, Opcodes.ICONST_0});
                FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                return new ClassField("QueueTraversed", f.name, f.desc);
            }
        }

        // Dec 11th, 2021
        // Used to be an array of bytes.
        // This field has changed to an array of some class containing a single byte field.
        for (FieldNode f : node.fields) {
            if (f.desc.startsWith("[L")) {
                ClassNode n = Main.getClass(Type.getType(f.desc).getElementType().getClassName());
                for (FieldNode ff : n.fields) {
                    if (ff.desc.equals("B")) {
                        return new ClassField("QueueTraversed", f.name, f.desc);
                    }
                }
            }
        }

        return new ClassField("QueueTraversed");
    }

    private ClassField findQueueLength(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.BIPUSH};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(IIZ)V") && hasAccess(m, Opcodes.ACC_FINAL)) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                    if (((IntInsnNode) m.instructions.get(i + 4)).operand == 0x9) {
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                        return new ClassField("QueueLength", f.name, f.desc, multi);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("QueueLength");
    }

    private ClassField findCombatCycle(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.IF_ICMPLE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;III)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);

                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);

                        if (f.desc.equals("I")) {
                            int multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("CombatCycle", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("CombatCycle");
    }

    private ClassField findInteractingIndex(ClassNode node) {
        ClassNode gameInstance = new GameInstance().find(Main.getClasses());
        if (gameInstance == null) {
            return new ClassField("InteractingIndex");
        }

        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.AALOAD, Opcodes.ASTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;L%s;)V", gameInstance.name, node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        long multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                        return new ClassField("InteractingIndex", f.name, f.desc, multi);
                    }
                }
            }
        }
        return new ClassField("InteractingIndex");
    }

    private ClassField findHealth(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.IFLE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;III)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        long multi = (int) ((LdcInsnNode)m.instructions.get(i + 2)).cst;
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        return new ClassField("Health", f.name, f.desc, multi);
                    }
                }
            }
        }
        return new ClassField("Health");
    }

    private ClassField findMaxHealth(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.IDIV};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;III)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        long multi = (int) ((LdcInsnNode)m.instructions.get(i + 2)).cst;
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        return new ClassField("MaxHealth", f.name, f.desc, multi);
                    }
                }
            }
        }
        return new ClassField("MaxHealth");
    }

    private ClassField findRotation(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.PUTFIELD};
        for (ClassNode n : Main.getClasses()) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(L%s;)V", node.name)) && hasAccess(m, Opcodes.ACC_STATIC)) {
                    int i = new Finder(m).findPattern(pattern);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 5);
                        long multi = Main.findMultiplier(f.owner, f.name);
                        return new ClassField("Rotation", f.name, f.desc, multi);
                    }
                }
            }
        }

        return new ClassField("Rotation");
    }

    private ClassField findIsWalking(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 0);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("IsWalking", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("IsWalking");
    }

    private ClassField findTargetIndex(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("()V")) {
                int i = new Finder(m).findNextInstruction(0, Opcodes.PUTFIELD, 22);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode) m.instructions.get(i);
                    long multi = Main.findMultiplier(f.owner, f.name);
                    return new ClassField("TargetIndex", f.name, f.desc, multi);
                }
            }
        }
        return new ClassField("TargetIndex");
    }

    private ClassField findCombatInfoList(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", Main.get("CombatInfoList")))) {
                return new ClassField("CombatInfoList", f.name, f.desc);
            }
        }
        return new ClassField("CombatInfoList");
    }

    private ClassField findCombatInfoHealthScale(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();

        ClassNode gameInstance = new GameInstance().find(nodes);
        if (gameInstance == null) {
            return new ClassField("HealthScale");
        }

        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.IDIV, Opcodes.ISTORE};
        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;L%s;IIIII)V", gameInstance.name, node.name))) {
                    int i = new Finder(m).findPattern(pattern);
                    while (i != -1) {
                        FieldInsnNode f = (FieldInsnNode) m.instructions.get(i + 1);
                        if (f.owner.equals(Main.get("CombatInfo2"))) {
                            long multi = Main.findMultiplier(f.owner, f.name);
                            return new ClassField("HealthScale", f.name, f.desc, multi);
                        }
                        i = new Finder(m).findPattern(pattern, i + 1);
                    }
                }
            }
        }
        return new ClassField("HealthScale");
    }

    private ClassField findSpotAnimation(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();

        ClassNode gameInstance = new GameInstance().find(nodes);
        if (gameInstance == null) {
            return new ClassField("SpotAnimation");
        }

        final int[] pattern = new int[]{
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL,
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISUB, Opcodes.ISTORE,
                Opcodes.GETSTATIC
        };

        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;L%s;)V", gameInstance.name, node.name))) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        FieldInsnNode ff = (FieldInsnNode)m.instructions.get(i + 5);
                        if (f.owner.equals(node.name) && ff.owner.equals(node.name)) {
                            int multi = (int) ((LdcInsnNode) m.instructions.get(i + 6)).cst;
                            return new ClassField("SpotAnimation", ff.name, ff.desc, multi);
                        }
                    }
                }

                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        FieldInsnNode ff = (FieldInsnNode)m.instructions.get(i + 5);
                        if (f.owner.equals(node.name) && ff.owner.equals(node.name)) {
                            int multi = (int) ((LdcInsnNode) m.instructions.get(i + 6)).cst;
                            return new ClassField("SpotAnimation", ff.name, ff.desc, multi);
                        }
                    }
                }
            }
        }
        return new ClassField("SpotAnimation");
    }

    private ClassField findSpotAnimationFrame(ClassNode node) {
        return new ClassField("SpotAnimationFrame");
    }

    private ClassField findSpotAnimationFrameCycle(ClassNode node) {
        Collection<ClassNode> nodes = Main.getClasses();

        ClassNode gameInstance = new GameInstance().find(nodes);
        if (gameInstance == null) {
            return new ClassField("SpotAnimationFrameCycle");
        }

        final int[] pattern = new int[]{
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL,
                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.ISUB, Opcodes.ISTORE,
                Opcodes.GETSTATIC
        };

        for (ClassNode n : nodes) {
            for (MethodNode m : n.methods) {
                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;L%s;)V", gameInstance.name, node.name))) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        FieldInsnNode ff = (FieldInsnNode)m.instructions.get(i + 5);
                        if (f.owner.equals(node.name) && ff.owner.equals(node.name)) {
                            int multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("SpotAnimationFrameCycle", f.name, f.desc, multi);
                        }
                    }
                }

                if (hasAccess(m, Opcodes.ACC_STATIC) && m.desc.equals(String.format("(L%s;)V", node.name))) {
                    int i = new Finder(m).findPattern(pattern, 0, false);
                    if (i != -1) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                        FieldInsnNode ff = (FieldInsnNode)m.instructions.get(i + 5);
                        if (f.owner.equals(node.name) && ff.owner.equals(node.name)) {
                            int multi = (int) ((LdcInsnNode) m.instructions.get(i + 2)).cst;
                            return new ClassField("SpotAnimationFrameCycle", f.name, f.desc, multi);
                        }
                    }
                }
            }
        }
        return new ClassField("SpotAnimationFrameCycle");
    }

    private ClassField findGraphicsId(ClassNode node) {
        return new ClassField("GraphicsId");
    }
}
