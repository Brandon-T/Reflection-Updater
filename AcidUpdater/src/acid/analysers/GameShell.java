package acid.analysers;

import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.LdcInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-22.
 */
public class GameShell extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/applet/Applet") || !hasAccess(n, Opcodes.ACC_ABSTRACT)) {
                continue;
            }
            return n;
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("GameShell", node.name);
        info.putField(findErrorMethod(node));
        info.putField(findStartMethod(node));
        info.putField(findStopMethod(node));
        info.putField(findDestroyMethod(node));
        info.putField(findPaintMethod(node));
        info.putField(findUpdateMethod(node));
        return info;
    }

    private ClassField findErrorMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.contains("(Ljava/lang/String;") && !hasAccess(m, Opcodes.ACC_STATIC)) {
                for (AbstractInsnNode a : m.instructions.toArray()) {
                    if (a.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode l = (LdcInsnNode)a;
                        if (l.cst instanceof String && ((String)l.cst).contains("error")) {
                            return new ClassField("*Error", m.name, m.desc);
                        }
                    }
                }
            }
        }
        return new ClassField("*Error");
    }

    private ClassField findStartMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("start")) {
                return new ClassField("*Start", m.name, m.desc);
            }
        }
        return new ClassField("*Start");
    }

    private ClassField findStopMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("stop")) {
                return new ClassField("*Stop", m.name, m.desc);
            }
        }
        return new ClassField("*Stop");
    }

    private ClassField findDestroyMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("destroy")) {
                return new ClassField("*Destroy", m.name, m.desc);
            }
        }
        return new ClassField("*Destroy");
    }

    private ClassField findPaintMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("paint")) {
                return new ClassField("*Paint", m.name, m.desc);
            }
        }
        return new ClassField("*Paint");
    }

    private ClassField findUpdateMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("update")) {
                return new ClassField("*Update", m.name, m.desc);
            }
        }
        return new ClassField("*Update");
    }
}
