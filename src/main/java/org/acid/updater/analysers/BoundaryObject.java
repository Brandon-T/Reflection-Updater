package org.acid.updater.analysers;

import org.acid.updater.Main;
import org.acid.updater.other.Finder;
import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Brandon on 2014-12-14.
 */
public class BoundaryObject extends Analyser {
    private MethodNode method = null;

    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        ClassNode n = Main.getClassNode("Scene");
        if (n != null) {
            for (MethodNode m : n.methods) {
                if (m.desc.equals(String.format("(IIIIL%s;L%s;IIJI)V", Main.get("Renderable"), Main.get("Renderable")))) {
                    for (AbstractInsnNode i : m.instructions.toArray()) {
                        if (i instanceof FieldInsnNode f) {
                            if (!f.owner.equals(n.name) && f.desc.equals("I") && !f.owner.matches("([ISBJZ])")) {
                                this.method = m;
                                return Main.getClass(f.owner);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("BoundaryObject", node.name);
        info.putField(findField(node, "ID", "getHash"));
        info.putField(findField(node, "Flags", "getConfig"));
        info.putField(findField(node, "Plane", "getPlane"));
        info.putField(findField(node, "Height", "getZ"));
        info.putField(findField(node, "X", "getX"));
        info.putField(findField(node, "Y", "getY"));
        info.putField(findField(node, "Renderable", "getRenderable1"));
        info.putField(findField(node, "OldRenderable", "getRenderable2"));
        return info;
    }

    private ClassField findRenderable(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.PUTFIELD};
        if (this.method != null) {
            int i = new Finder(this.method).findPattern(pattern);
            while (i != -1) {
                if (((VarInsnNode) method.instructions.get(i + 1)).var == 5) {
                    FieldInsnNode f = (FieldInsnNode) method.instructions.get(i + 2);
                    return new ClassField("Renderable", f.name, f.desc);
                }
                i = new Finder(this.method).findPattern(pattern, i + 1);
            }
        }
        return new ClassField("Renderable");
    }

    private ClassField findOldRenderable(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.PUTFIELD};
        if (this.method != null) {
            int i = new Finder(this.method).findPattern(pattern);
            while (i != -1) {
                if (((VarInsnNode) method.instructions.get(i + 1)).var == 6) {
                    FieldInsnNode f = (FieldInsnNode) method.instructions.get(i + 2);
                    return new ClassField("OldRenderable", f.name, f.desc);
                }
                i = new Finder(this.method).findPattern(pattern, i + 1);
            }
        }
        return new ClassField("OldRenderable");
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
