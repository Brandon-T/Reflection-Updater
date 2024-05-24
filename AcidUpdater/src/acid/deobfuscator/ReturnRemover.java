package acid.deobfuscator;

import acid.other.DeprecatedFinder;
import acid.other.Finder;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.JumpInsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Kira on 2015-01-11.
 */
public class ReturnRemover extends Deobfuscator {
    private int total_count = 0, removal_count = 0;

    public ReturnRemover(Collection<ClassNode> classes) {
        super(classes);
    }

    @Override
    public Deobfuscator analyse() {
        classes.stream().forEach(c -> c.methods.stream().forEach(this::countReturns));
        return this;
    }

    @Override
    public void remove() {
        classes.stream().forEach(c -> c.methods.stream().forEach(this::removeReturns));
        System.out.println("Removed Returns: " + removal_count + " of " + total_count);
    }

    private void countReturns(MethodNode method) {
        int patterns[] = new int[]{Opcodes.RET, Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN, Opcodes.RETURN};

        for (int pattern : patterns) {
            int i = new DeprecatedFinder(method).findNext(0, pattern);
            while (i != -1) {
                ++total_count;
                i = new DeprecatedFinder(method).findNext(i + 1, pattern);
            }
        }
    }

    private void removeReturns(MethodNode method) {
        int patterns[][] = new int[][]{
                {Opcodes.ILOAD, Finder.CONSTANT, Finder.COMPARISON},
        };

        for (int[] pattern : patterns) {
            findReplaceException(method, pattern);
        }
    }

    private void findReplaceException(MethodNode method, int[] pattern) {
        int i = new Finder(method).findPattern(pattern);
        while (i != -1) {
            if (isReturn(method.instructions.get(i + pattern.length).getOpcode())) {
                LabelNode jmp = findNextJump(method, i, pattern.length);
                if (jmp != null) {
                    method.instructions.insertBefore(method.instructions.get(i), new JumpInsnNode(Opcodes.GOTO, jmp));
                    ++i;
                }

                for (int j = 0; j < pattern.length; ++j) {
                    method.instructions.remove(method.instructions.get(i));
                }

                ++removal_count;
            }
            i = new Finder(method).findPattern(pattern, i + 1);
        }
    }

    private LabelNode findNextJump(MethodNode method, int offset, int maxLength) {
        for (int i = 0; i < maxLength; ++i) {
            if (method.instructions.get(i + offset) instanceof JumpInsnNode) {
                return ((JumpInsnNode)method.instructions.get(i + offset)).label;
            }
        }
        return null;
    }

    private boolean isReturn(int opcode) {
        return (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.RET;
    }
}
