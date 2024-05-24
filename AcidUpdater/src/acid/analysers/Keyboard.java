package acid.analysers;

import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-23.
 */
public class Keyboard extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.superName.equals("java/lang/Object")) {
                continue;
            }

            if (!n.interfaces.contains("java/awt/event/KeyListener") || !n.interfaces.contains("java/awt/event/FocusListener")) {
                continue;
            }

            return n;
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("Keyboard", node.name);
        info.putField(findKeyPressed(node));
        info.putField(findKeyReleased(node));
        info.putField(findKeyTyped(node));
        info.putField(findFocusGained(node));
        info.putField(findFocusLost(node));
        return info;
    }

    private ClassField findKeyPressed(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("keyPressed")) {
                return new ClassField("*KeyPressed", m.name, m.desc);
            }
        }
        return new ClassField("*KeyPressed");
    }

    private ClassField findKeyReleased(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("keyReleased")) {
                return new ClassField("*KeyReleased", m.name, m.desc);
            }
        }
        return new ClassField("*KeyReleased");
    }

    private ClassField findKeyTyped(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("keyTyped")) {
                return new ClassField("*KeyTyped", m.name, m.desc);
            }
        }
        return new ClassField("*KeyTyped");
    }

    private ClassField findFocusGained(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("focusGained")) {
                return new ClassField("*FocusGaiend", m.name, m.desc);
            }
        }
        return new ClassField("*FocusGained");
    }

    private ClassField findFocusLost(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.name.equals("focusLost")) {
                return new ClassField("*FocusLost", m.name, m.desc);
            }
        }
        return new ClassField("*FocusLost");
    }
}
