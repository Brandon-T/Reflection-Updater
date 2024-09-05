package acid.analysers;

import acid.other.Finder;
import acid.structures.ClassField;
import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-21.
 */
public class BufferedConnection extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.interfaces.contains("java/lang/Runnable")) {
                continue;
            }

            int isCount = 0, osCount = 0, sockCount = 0;
            for (FieldNode f : n.fields) {
                if (f.desc.equals("Ljava/io/InputStream;")) {
                    ++isCount;
                } else if (f.desc.equals("Ljava/io/OutputStream;")) {
                    ++osCount;
                } else if (f.desc.equals("Ljava/net/Socket;")) {
                    ++sockCount;
                }
            }

            if (isCount == 1 && osCount == 1 && sockCount == 1) {
                return n;
            }
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("BufferedConnection", node.name);
        info.putField(findInputStream(node));
        info.putField(findOutputStream(node));
        info.putField(findSocket(node));
        info.putField(findPayload(node));
        info.putField(findClosed(node));
        info.putField(findAvailableMethod(node));
        info.putField(findReadMethod(node));
        info.putField(findWriteMethod(node));
        return info;
    }

    private ClassField findInputStream(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("Ljava/io/InputStream;")) {
                return new ClassField("InputStream", f.name, f.desc);
            }
        }
        return new ClassField("InputStream");
    }

    private ClassField findOutputStream(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("Ljava/io/OutputStream;")) {
                return new ClassField("OutputStream", f.name, f.desc);
            }
        }
        return new ClassField("OutputStream");
    }

    private ClassField findSocket(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("Ljava/net/Socket;")) {
                return new ClassField("Socket", f.name, f.desc);
            }
        }
        return new ClassField("Socket");
    }

    private ClassField findPayload(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.INVOKEVIRTUAL};
        for (MethodNode m : node.methods) {
            if (m.name.equals("run") && m.desc.equals("()V")) {
                int i = new Finder(m).findPattern(pattern);
                while(i != -1) {
                    if (((MethodInsnNode)m.instructions.get(i + 5)).name.equals("write")) {
                        FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 2);
                        return new ClassField("Payload", f.name, f.desc);
                    }
                    i = new Finder(m).findPattern(pattern, i + 1);
                }
            }
        }
        return new ClassField("Payload");
    }

    private ClassField findClosed(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IFEQ};
        for (MethodNode m : node.methods) {
            if (m.exceptions.contains("java/io/IOException") && m.desc.equals("([BII)V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    FieldInsnNode f = (FieldInsnNode)m.instructions.get(i + 1);
                    if (f.owner.equals(node.name)) {
                        return new ClassField("IsClosed", f.name, f.desc);
                    }
                }
            }
        }
        return new ClassField("IsClosed");
    }

    private ClassField findAvailableMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.exceptions.contains("java/io/IOException") && m.desc.equals("()I")) {
                for (AbstractInsnNode a : m.instructions.toArray()) {
                    if (a instanceof MethodInsnNode) {
                        MethodInsnNode n = (MethodInsnNode)a;
                        if (n.name.equals("available")) {
                            return new ClassField("*Available", m.name, m.desc);
                        }
                    }
                }
            }
        }
        return new ClassField("*Available");
    }

    private ClassField findReadMethod(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.exceptions.contains("java/io/IOException") && m.desc.equals("([BII)V")) {
                for (AbstractInsnNode a : m.instructions.toArray()) {
                    if (a instanceof MethodInsnNode) {
                        MethodInsnNode n = (MethodInsnNode)a;
                        if (n.name.equals("read")) {
                            return new ClassField("*Read", m.name, m.desc);
                        }
                    }
                }
            }
        }
        return new ClassField("*Read");
    }

    private ClassField findWriteMethod(ClassNode node) {
        final int[] pattern = new int[]{Opcodes.ALOAD, Opcodes.SIPUSH, Opcodes.NEWARRAY, Opcodes.PUTFIELD};
        for (MethodNode m : node.methods) {
            if (m.exceptions.contains("java/io/IOException") && m.desc.equals("([BII)V")) {
                int i = new Finder(m).findPattern(pattern);
                if (i != -1) {
                    return new ClassField("*Write", m.name, m.desc);
                }
            }
        }
        return new ClassField("*Write");
    }
}
