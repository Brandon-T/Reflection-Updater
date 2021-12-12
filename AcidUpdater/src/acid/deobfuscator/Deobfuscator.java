package acid.deobfuscator;

import acid.other.DeprecatedFinder;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Kira on 2015-01-09.
 */
public abstract class Deobfuscator {
    protected final Collection<ClassNode> classes;

    /**
     * Analyses and Deobfuscates a given collection of classes.
     * @param classes  Collection of classes to be analysed and deobfuscated.
     */
    public Deobfuscator(Collection<ClassNode> classes) {
        this.classes = classes;
    }

    /**
     * Analyses the collection of classes for patterns to be changed or removed.
     * @return Returns an instance of "this".
     */
    public abstract Deobfuscator analyse();

    /**
     * Changes or Removes all patterns found during analysing from the collection of classes.
     */
    public abstract void remove();

    /**
     * Moves an instruction from "moveFrom" to "moveTo" for each instance of "pattern"
     * found in "method".
     *
     * @param method  Method to search.
     * @param pattern  Pattern to search for.
     * @param moveFrom  Index of the instruction to move.
     * @param moveTo  Index to place the new instruction.
     * @return
     */
    protected int moveInstructions(MethodNode method, int[] pattern, int moveFrom, int moveTo) {
        int total_moved = 0;
        ArrayList<AbstractInsnNode> instructions = new ArrayList<>(Arrays.asList(method.instructions.toArray()));

        int i = new DeprecatedFinder(method).findPattern(pattern);
        while (i != -1) {
            instructions.add(i + moveTo, instructions.get(i + moveFrom));
            instructions.remove(i + moveFrom);
            ++total_moved;
            i = new DeprecatedFinder(method).findPattern(pattern, i + 1);
        }
        method.instructions.clear();
        instructions.stream().forEachOrdered(e -> method.instructions.add(e));
        return total_moved;
    }

    /**
     * Moves an instruction from "moveFrom" to "moveTo" for each instance of "pattern to last"
     * found in "method". After finding "pattern", this function finds the "next" instruction
     * and performs the movement.
     *
     * @param method  Method to search.
     * @param pattern  Pattern to search for.
     * @param next  Next instruction to be found anywhere after the occurrence of "pattern".
     * @param moveFrom  Index of the instruction to move.
     * @param moveTo  Index to place the new instruction.
     * @return
     */
    protected int moveInstructions(MethodNode method, int[] pattern, int next, int moveFrom, int moveTo) {
        int total_moved = 0;
        ArrayList<AbstractInsnNode> instructions = new ArrayList<>(Arrays.asList(method.instructions.toArray()));

        int i = new DeprecatedFinder(method).findPattern(pattern);
        while (i != -1) {
            int j = new DeprecatedFinder(method).findNext(i + pattern.length, next);
            if (j != -1) {
                instructions.add(i + moveTo + (j - i), instructions.get(i + moveFrom));
                instructions.remove(i + moveFrom);
                ++total_moved;
            }
            i = new DeprecatedFinder(method).findPattern(pattern, i + 1);
        }
        method.instructions.clear();
        instructions.stream().forEachOrdered(e -> method.instructions.add(e));
        return total_moved;
    }
}
