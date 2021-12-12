package acid.analysers;

import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldInsnNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-14.
 */
public class Region extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object") || hasAccess(n, Opcodes.ACC_FINAL)) {
                continue;
            }

            for (FieldNode f : n.fields) {
                if (f.desc.equals("[[[[Z")) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Region", node.name);
        info.putField(findTiles(node));
        info.putField(findInteractableObjects(node));
        return info;
    }

    private ClassField findTiles(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.MULTIANEWARRAY, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("(III[[[I)V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 5);
                    return new ClassField("Tiles", f.name, f.desc);
                }
            }
        }
        return new ClassField("Tiles");
    }

    private ClassField findInteractableObjects(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.SIPUSH, Opcodes.ANEWARRAY, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.name.equals("<init>") && m.desc.equals("(III[[[I)V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 3);
                    return new ClassField("InteractableObjects", f.name, f.desc);
                }
            }
        }
        return new ClassField("InteractableObjects");
    }
}
