package acid.analysers;

import acid.Main;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-06.
 */
public abstract class Analyser {
    public abstract ClassNode find(Collection<ClassNode> nodes);
    public abstract ClassInfo analyse(ClassNode node);

    public boolean hasAccess(ClassNode node, int access) {
        return (node.access & access) != 0;
    }

    public boolean hasAccess(MethodNode node, int access) {
        return (node.access & access) != 0;
    }

    public boolean hasAccess(FieldNode node, int access) {
        return (node.access & access) != 0;
    }

    public boolean hasOwner(ClassNode node, String owner) {
        return findSuperField(node, owner);
    }

    private final boolean findSuperField(ClassNode node, String owner) {
        ClassNode n = node;
        while(n != null && !n.superName.equals("java/lang/Object") && !n.superName.contains("java")) {
            if (n.superName.equals(owner)) {
                return true;
            }

            n = findClass(n.superName);
        }
        return false;
    }

    private final ClassNode findClass(String name) {
        for (ClassNode n : Main.getClasses()) {
            if (n.name.equals(name)) {
                return n;
            }
        }
        return null;
    }
}
