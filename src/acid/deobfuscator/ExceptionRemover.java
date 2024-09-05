package acid.deobfuscator;

import acid.other.DeprecatedFinder;
import acid.other.Finder;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-23.
 */
public class ExceptionRemover extends Deobfuscator {
    private int exception_count = 0, removal_count = 0;

    public ExceptionRemover(Collection<ClassNode> classes) {
        super(classes);
    }

    public ExceptionRemover analyse() {
        classes.stream().forEach(n -> n.methods.forEach(m -> m.instructions.iterator().forEachRemaining(a -> {
            if (a.getOpcode() == Opcodes.INVOKESPECIAL) {
                MethodInsnNode e = (MethodInsnNode) a;
                if (e.name.contains("Exception") || e.owner.contains("Exception")) {
                    ++exception_count;
                }
            }
        })));
        return this;
    }

    public void remove() {
        classes.stream().forEach(n -> n.methods.forEach(this::removeExceptions));
        System.out.println("Removed Exceptions: " + removal_count + " of " + exception_count);
    }

    private void removeExceptions(MethodNode method) {
        int patterns[][] = new int[][]{
                {Opcodes.ILOAD, Finder.CONSTANT, Finder.COMPARISON, Opcodes.NEW, Opcodes.DUP, Opcodes.INVOKESPECIAL, Opcodes.ATHROW}
        };

        for (int[] pattern : patterns) {
            findReplaceException(method, pattern, "Exception");
        }
    }

    private void findReplaceException(MethodNode method, int[] pattern, String exceptionName) {
        int i = findNextException(method, pattern, exceptionName);
        while (i != -1) {
            LabelNode jmp = findNextJump(method, i, pattern.length);
            if (jmp != null) {
                method.instructions.insertBefore(method.instructions.get(i), new JumpInsnNode(Opcodes.GOTO, jmp));
                ++i;
            }

            for (int j = 0; j < pattern.length; ++j) {
                method.instructions.remove(method.instructions.get(i));
            }

            i = findNextException(method, pattern, exceptionName);
            ++removal_count;
        }
    }

    private int findNextException(MethodNode method, int[] pattern, String exceptionName) {
        int i = new DeprecatedFinder(method).findPattern(pattern, 0, true);
        while (i != -1) {
            for (int j = i; j < i + pattern.length; ++j) {
                if (method.instructions.get(j) instanceof MethodInsnNode) {
                    if (((MethodInsnNode) method.instructions.get(j)).owner.contains(exceptionName)) {
                        return i;
                    }
                }
            }
            i = new DeprecatedFinder(method).findPattern(pattern, i + 1, true);
        }
        return -1;
    }

    private LabelNode findNextJump(MethodNode method, int offset, int maxLength) {
        for (int i = 0; i < maxLength; ++i) {
            if (method.instructions.get(i + offset) instanceof JumpInsnNode) {
                return ((JumpInsnNode)method.instructions.get(i + offset)).label;
            }
        }
        return null;
    }
}