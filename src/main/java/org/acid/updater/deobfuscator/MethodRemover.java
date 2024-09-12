package org.acid.updater.deobfuscator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Brandon on 2014-12-09.
 */
public class MethodRemover extends Deobfuscator {
    private final ArrayList<Info> usedMethods;
    private int total_count = 0, removal_count = 0;

    public MethodRemover(Collection<ClassNode> classes) {
        super(classes);
        this.usedMethods = new ArrayList<>();
    }

    @Override
    public Deobfuscator analyse() {
        classes.forEach(c -> c.methods.forEach(m -> ++total_count));
        countUsedMethods();
        return this;
    }

    @Override
    public void remove() {
        removeUnusedMethods();
        int last_count = removal_count;
        while (removal_count > 0) {
            removal_count = 0;
            usedMethods.clear();
            countUsedMethods();
            removeUnusedMethods();
            last_count += removal_count;
        }
        System.out.println("Removed Methods: " + last_count + " of " + total_count);
    }

    private void countUsedMethods() {
        classes.forEach(c -> c.methods.forEach(m -> {
            if (isAbstract(m) || isConstructor(m) || isOverriden(c, m) || isInterface(c, m)) {
                checkAdd(c, m);
            }

            addInvoked(m);
        }));
    }

    private void removeUnusedMethods() {
        classes.stream().forEach(n -> n.methods.removeIf((entry) -> {
            if (!usedMethods.contains(new Info(n, entry))) {
                ++removal_count;
                return true;
            }
            return false;
        }));
    }

    private boolean isAbstract(MethodNode method) {
        return (method.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    private boolean isConstructor(MethodNode method) {
        return method.name.equals("<init>") || method.name.equals("<clinit>");
    }

    public boolean isInterface(ClassNode node, MethodNode method) {
        for (String i : node.interfaces) {
            if (hasMethod(loadAnyClass(i), method)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverriden(ClassNode node, MethodNode method) {
        ClassNode n = node;
        while (true) {
            n = loadAnyClass(n.superName);
            for (MethodNode m : n.methods) {
                if (m.name.equals(method.name) && m.desc.equals(method.desc)) {
                    return true;
                }
            }

            if (n.name.equals("java/lang/Object")) {
                break;
            }
        }
        return false;
    }

    private void addInvoked(MethodNode method) {
        method.instructions.iterator().forEachRemaining(i -> {
            if (i instanceof MethodInsnNode mi) {
                if (!mi.owner.contains("java") && !mi.name.equals("<init>") && !mi.name.equals("<clinit>") && !mi.owner.startsWith("[")) {
                    ClassNode owner = loadClass(mi.owner);
                    MethodNode custom_method = new MethodNode(Opcodes.ASM5, method.access, mi.name, mi.desc, null, null);
                    if (hasMethod(owner, custom_method)) {
                        checkAdd(owner, custom_method);
                    } else {
                        while (!owner.superName.contains("java")) {
                            owner = loadClass(owner.superName);
                            if (hasMethod(owner, custom_method)) {
                                checkAdd(owner, custom_method);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }


    private void checkAdd(ClassNode node, MethodNode method) {
        Info info = new Info(node, method);
        if (!usedMethods.contains(info)) {
            usedMethods.add(info);
        }
    }

    private boolean hasMethod(ClassNode node, MethodNode method) {
        for (MethodNode m : node.methods) {
            if (m.name.equals(method.name) && m.desc.equals(method.desc)) {
                return true;
            }
        }
        return false;
    }

    private ClassNode loadClass(String name) {
        for (ClassNode n : classes) {
            if (n.name.equals(name)) {
                return n;
            }
        }
        return null;
    }

    private ClassNode loadAnyClass(String name) {
        if (name.startsWith("java")) {
            try {
                ClassNode n = new ClassNode();
                new ClassReader(name).accept(n, 0);
                return n;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return loadClass(name);
    }


    private class Info {
        private final String node;
        private final String name;
        private final String desc;

        public Info(ClassNode node, MethodNode method) {
            this.node = node.name;
            this.name = method.name;
            this.desc = method.desc;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Info info) {
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
