package acid.analysers;

import acid.Main;
import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-07.
 */
public class Stream extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals(Main.get("Node"))) {
                continue;
            }

            for (MethodNode m : n.methods) {
                if (m.name.equals("<init>") && m.desc.equals("([B)V")) {
                    if (new Finder(m).findPattern(new int[]{Opcodes.ALOAD, Opcodes.ICONST_0, Opcodes.PUTFIELD}) != -1) {
                        return n;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Stream", node.name);
        info.putField(findPayload(node));
        info.putField(findCRCTable(node));
        info.putField(findApplyRSAMethod(node));
        info.putField(findGetUnsignedByteMethod(node));
        return info;
    }

    private ClassField findPayload(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[B") && !hasAccess(f, Opcodes.ACC_STATIC)) {
                return new ClassField("Payload", f.name, f.desc);
            }
        }
        return new ClassField("Payload");
    }

    private ClassField findCRCTable(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("[I") && hasAccess(f, Opcodes.ACC_STATIC)) {
                return new ClassField("CRC", f.name, f.desc);
            }
        }
        return new ClassField("CRC");
    }

    private ClassField findApplyRSAMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(Ljava/math/BigInteger;Ljava/math/BigInteger;)V")) {
                return new ClassField("*ApplyRSA", m.name, m.desc);
            }
        }
        return new ClassField("*ApplyRSA");
    }

    private ClassField findGetUnsignedByteMethod(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.LDC, Opcodes.IMUL, Opcodes.ICONST_1, Finder.WILDCARD, Finder.WILDCARD, Opcodes.SIPUSH};
        for (MethodNode m : node.methods) {
            if (m.desc.equals("()I")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    if (((IntInsnNode)m.instructions.get(i + 5)).operand == 0xFF) {
                        return new ClassField("*GetUnsignedByte", m.name, m.desc);
                    }
                }
            }
        }
        return new ClassField("*GetUnsignedByte");
    }
}
