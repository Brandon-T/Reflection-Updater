package org.acid.updater.deprecated;

import org.acid.updater.deobfuscator.Deobfuscator;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Kira on 2014-12-10.
 */

@Deprecated
public class FieldRemover extends Deobfuscator {
    private ArrayList<String> usedFields;
    int field_count = 0;

    public FieldRemover(Collection<ClassNode> classes) {
        super(classes);
        this.usedFields = new ArrayList<>();
    }

    @Override
    public Deobfuscator analyse() {
        classes.stream().forEach(n -> n.fields.stream().forEach(f -> {
            if (hasField(n, f)) {
                if (!this.usedFields.contains(n.name + "." + f.name)) {
                    this.usedFields.add(n.name + "." + f.name);
                }
            }
            ++field_count;
        }));
        return this;
    }

    @Override
    public void remove() {
        classes.stream().forEach((entry) -> entry.fields.removeIf((field) -> !usedFields.contains(entry.name + "." + field.name)));
        System.out.println("Kept Fields: " + usedFields.size() + " of " + field_count);
    }

    private boolean hasField(ClassNode node, FieldNode field) {
        for (ClassNode n : classes) {
            for (MethodNode m : n.methods) {
                for (AbstractInsnNode a : m.instructions.toArray()) {
                    if (a instanceof FieldInsnNode) {
                        FieldInsnNode f = (FieldInsnNode)a;
                        if (f.owner.equals(node.name) && f.name.equals(field.name)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
