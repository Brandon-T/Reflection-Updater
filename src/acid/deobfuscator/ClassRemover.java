package acid.deobfuscator;

import jdk.internal.org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Kira on 2014-12-10.
 */
public class ClassRemover extends Deobfuscator {
    private ArrayList<String> usedClasses;
    private int class_count = 0;

    public ClassRemover(Collection<ClassNode> classes) {
        super(classes);
        this.usedClasses = new ArrayList<>();
        this.class_count = classes.size();
    }

    public ClassRemover analyse() {
        classes.stream().forEach(node -> {
            start:
            for (ClassNode n : classes) {
                checkFields(n);
                for (MethodNode m : n.methods) {
                    if (checkInvocation(node, m)) {
                        break start;
                    }
                }
            }
        });
        return this;
    }

    public void remove() {
        classes.removeIf((entry) -> !usedClasses.contains(entry.name));
        System.out.println("Kept Classes: " + usedClasses.size() + " of " + class_count);
    }

    private boolean checkFields(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals(String.format("L%s;", node.name))) {
                if (!usedClasses.contains(node.name)) {
                    usedClasses.add(node.name);
                }
                return true;
            }
        }
        return false;
    }

    private boolean checkInvocation(ClassNode node, MethodNode method) {
        AbstractInsnNode[] instructions = method.instructions.toArray();
        for (AbstractInsnNode n : instructions) {
            if (n instanceof FieldInsnNode) {
                FieldInsnNode f = (FieldInsnNode)n;
                if (f.owner.equals(node.name)) {
                    if (!usedClasses.contains(node.name)) {
                        usedClasses.add(node.name);
                    }
                    return true;
                }
            } else if (n instanceof MethodInsnNode) {
                MethodInsnNode m = (MethodInsnNode)n;
                if (m.owner.equals(node.name)) {
                    if (!usedClasses.contains(node.name)) {
                        usedClasses.add(node.name);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
