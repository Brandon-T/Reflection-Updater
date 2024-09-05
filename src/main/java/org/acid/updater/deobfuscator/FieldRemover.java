package org.acid.updater.deobfuscator;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Kira on 2015-01-13.
 */
public class FieldRemover extends Deobfuscator {
    private ArrayList<Info> usedFields;
    private int total_count = 0, removal_count = 0;

    public FieldRemover(Collection<ClassNode> classes) {
        super(classes);
        this.usedFields = new ArrayList<>();
    }

    @Override
    public Deobfuscator analyse() {
        countUsedFields();
        total_count = usedFields.size();
        return this;
    }

    @Override
    public void remove() {
        removeUnusedFields();
        int last_count = removal_count;
        while(removal_count > 0) {
            removal_count = 0;
            usedFields.clear();
            countUsedFields();
            removeUnusedFields();
            last_count += removal_count;
        }
        System.out.println("Removed Fields: " + last_count + " of " + total_count);
    }


    private void countUsedFields() {
        classes.stream().forEach(c -> c.methods.stream().forEach(m -> m.instructions.iterator().forEachRemaining(i -> {
            if (i instanceof FieldInsnNode) {
                FieldInsnNode f = (FieldInsnNode) i;
                checkAdd(f.owner, f.name, f.desc);
            }
        })));
    }

    private void removeUnusedFields() {
        classes.stream().forEach(c -> c.fields.removeIf((entry) -> {
            if (!usedFields.contains(new Info(c.name, entry.name, entry.desc))) {
                ++removal_count;
                return true;
            }
            return false;
        }));
    }


    private void checkAdd(String node, String name, String desc) {
        Info info = new Info(node, name, desc);
        if (!usedFields.contains(info)) {
            usedFields.add(info);
        }
    }


    private class Info {
        private String node, name, desc;

        public Info(String node, String name, String desc) {
            this.node = node;
            this.name = name;
            this.desc = desc;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Info) {
                Info info = (Info) o;
                return node.equals(info.node) && name.equals(info.name) && desc.equals(info.desc);
            }
            return false;
        }

        @Override
        public String toString() {
            return node + "." + name + "   " + desc;
        }
    }
}
