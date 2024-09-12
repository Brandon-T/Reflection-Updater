package org.acid.updater.other;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Brandon on 2015-01-08.
 */
public class Renamer {
    private final Collection<ClassNode> classes;

    public Renamer(Collection<ClassNode> classes) {
        this.classes = classes;
    }


    public void renameClass(String node, String newName) {
        classes.stream().forEach(n -> {
            if (n.signature != null) {
                n.signature = n.signature.replace(node, newName);
            }

            if (n.name.equals(node)) {
                n.name = newName;
            }

            if (n.superName.equals(node)) {
                n.superName = newName;
            }

            for (int i = 0; i < n.interfaces.size(); ++i) {
                n.interfaces.set(i, n.interfaces.get(i).replace(node, newName));
            }

            n.innerClasses.stream().forEach(i -> {
                if (i.name.equals(node)) {
                    i.name = newName;
                }

                if (i.innerName != null && i.innerName.equals(node)) {
                    i.innerName = newName;
                }

                if (i.outerName != null && i.outerName.equals(node)) {
                    i.outerName = newName;
                }
            });

            n.fields.stream().forEach(f -> {
                f.desc = f.desc.replace("L" + node + ";", "L" + newName + ";");
            });

            n.methods.stream().forEach(m -> {
                m.desc = m.desc.replace("L" + node + ";", "L" + newName + ";");

                if (m.signature != null) {
                    m.signature = m.signature.replace(node, newName);
                }

                for (int i = 0; i < m.exceptions.size(); ++i) {
                    if (m.exceptions.get(i).equals(node)) {
                        m.exceptions.set(i, m.exceptions.get(i).replace(node, newName));
                    }
                }

                if (m.localVariables != null) {
                    m.localVariables.stream().forEach(l -> {
                        l.desc = l.desc.replace("L" + node + ";", "L" + newName + ";");
                    });
                }


                m.instructions.iterator().forEachRemaining(o -> {
                    if (o instanceof TypeInsnNode t) {
                        if (t.desc.equals(node)) {
                            t.desc = newName;
                        }
                    } else if (o instanceof FieldInsnNode f) {
                        f.desc = f.desc.replace("L" + node + ";", "L" + newName + ";");
                        if (f.owner.equals(node)) {
                            f.owner = newName;
                        }
                    } else if (o instanceof MethodInsnNode mi) {
                        mi.desc = mi.desc.replace("L" + node + ";", "L" + newName + ";");
                        if (mi.owner.equals(node)) {
                            mi.owner = newName;
                        }
                    }
                });
            });
        });
    }

    public void renameField(String owner, String name, String desc, String newName, String newDesc) {
        List<String> classNames = new ArrayList<>();

        classes.stream().forEach(n -> {
            if (isChildOf(owner, n)) {
                classNames.add(n.name);

                n.fields.stream().forEach(f -> {
                    if (f.name.equals(name)) {
                        f.name = newName;
                        if (newDesc != null) {
                            f.desc = newDesc;
                        }
                    }
                });
            }

            n.methods.stream().forEach(m -> {
                m.instructions.iterator().forEachRemaining(o -> {
                    if (o instanceof FieldInsnNode f) {
                        if (classNames.contains(f.owner) && f.name.equals(name)) {
                            f.name = newName;
                            if (newDesc != null) {
                                f.desc = newDesc;
                            }
                        }
                    }
                });
            });
        });
    }

    public void renameMethod(String owner, String name, String desc, String newName, String newDesc) {
        classes.stream().forEach(n -> {
            n.methods.stream().forEach(m -> {
                m.instructions.iterator().forEachRemaining(o -> {
                    if (o instanceof MethodInsnNode mi) {
                        if (mi.owner.equals(owner) && mi.name.equals(name) && mi.desc.equals(desc)) {
                            mi.name = newName;
                            if (newDesc != null) {
                                mi.desc = newDesc;
                            }
                        }
                    }
                });

                if (m.signature != null) {
                    m.signature = m.signature.replace(name, newName);
                }

                if (n.name.equals(owner) && m.name.equals(name) && m.desc.equals(desc)) {
                    m.name = newName;
                    if (newDesc != null) {
                        m.desc = newDesc;
                    }
                }
            });
        });
    }

    private boolean isChildOf(String parent, ClassNode current) {
        if (current == null) {
            return false;
        }

        if (current.name.equals(parent)) {
            return true;
        }

        while (current != null) {
            if (current.name.equals(parent) || current.superName.equals(parent)) {
                return true;
            }

            current = loadAnyClass(current.superName);
            if (current != null && current.name.equals("java/lang/Object")) {
                break;
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
}