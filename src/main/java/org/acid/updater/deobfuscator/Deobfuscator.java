package org.acid.updater.deobfuscator;

import org.acid.updater.other.DeprecatedFinder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Brandon on 2015-01-09.
 */
public abstract class Deobfuscator {
    protected final Collection<ClassNode> classes;

    /**
     * Analyses and Deobfuscates a given collection of classes.
     *
     * @param classes Collection of classes to be analysed and deobfuscated.
     */
    public Deobfuscator(Collection<ClassNode> classes) {
        this.classes = classes;
    }

    /**
     * Analyses the collection of classes for patterns to be changed or removed.
     *
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
     * @param method   Method to search.
     * @param pattern  Pattern to search for.
     * @param moveFrom Index of the instruction to move.
     * @param moveTo   Index to place the new instruction.
     * @return
     */
    protected int moveInstructions(MethodNode method, int[] pattern, int moveFrom, int moveTo) {
        int total_moved = 0;
        ArrayList<AbstractInsnNode> instructions = new ArrayList<>(Arrays.asList(method.instructions.toArray()));

        int i = new DeprecatedFinder(method).findPattern(pattern, 0, true);
        while (i != -1) {
            instructions.add(i + moveTo, instructions.get(i + moveFrom));
            instructions.remove(i + moveFrom + (moveTo < moveFrom ? 1 : 0));
            ++total_moved;
            i = new DeprecatedFinder(method).findPattern(pattern, i + 1, true);
        }
        method.instructions.clear();
        instructions.stream().forEachOrdered(e -> method.instructions.add(e));
        return total_moved;
    }

    /**
     * Moves a set of instructions from "moveFrom" to "moveTo" for each instance of "pattern"
     * found in "method".
     *
     * @param method   Method to search.
     * @param pattern  Pattern to search for.
     * @param length   Amount of instructions to move.
     * @param moveFrom Index of the instructions to move.
     * @param moveTo   Index to place the new instructions.
     * @return
     */
    protected int moveInstructions(MethodNode method, int[] pattern, int length, int moveFrom, int moveTo) {
        int total_moved = 0;
        ArrayList<AbstractInsnNode> instructions = new ArrayList<>(Arrays.asList(method.instructions.toArray()));

        int i = new DeprecatedFinder(method).findPattern(pattern, 0, true);
        while (i != -1) {
            for (int j = 0, offset = 0; j < length; ++j, ++offset) {
                instructions.add(i + j + moveTo, instructions.get(i + j + moveFrom + (moveTo < moveFrom ? offset : 0)));
            }

            for (int j = length - 1; j >= 0; --j) {
                instructions.remove(i + j + moveFrom + (moveTo < moveFrom ? length : 0));
                ++total_moved;
            }

            i = new DeprecatedFinder(method).findPattern(pattern, i + 1, true);
        }
        method.instructions.clear();
        instructions.stream().forEachOrdered(e -> method.instructions.add(e));
        return total_moved;
    }
}
