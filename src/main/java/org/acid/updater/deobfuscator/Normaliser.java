package org.acid.updater.deobfuscator;

import org.acid.updater.other.DeprecatedFinder;
import org.acid.updater.other.Finder;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Kira on 2015-01-09.
 */
public class Normaliser extends Deobfuscator {
    private int multi_total = 0;
    private int multi_count = 0;
    private int arithmetic_total = 0;
    private int arithmetic_count = 0;

    public Normaliser(Collection<ClassNode> classes) {
        super(classes);
    }

    @Override
    public Deobfuscator analyse() {
        classes.stream().forEach(c -> c.methods.stream().forEach(this::countMultipliers));
        classes.stream().forEach(c -> c.methods.stream().forEach(this::countArithmetic));
        return this;
    }

    @Override
    public void remove() {
        classes.stream().forEach(c -> c.methods.stream().forEach(this::reorderMultipliers));
        classes.stream().forEach(c -> c.methods.stream().forEach(this::reorderArithmetic));
        System.out.println("Re-Ordered Multipliers: " + multi_count + " of " + multi_total);
        System.out.println("Re-Ordered Arithmetics: " + arithmetic_count + " of " + arithmetic_total);
    }


    private void reorderMultipliers(MethodNode method) {
        //Move(pattern[0], pattern[pattern.length - 1]);
        int patterns[][] = new int[][]{
                {Opcodes.LDC, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.ALOAD, Opcodes.GETSTATIC, Opcodes.GETSTATIC, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.GETSTATIC, Opcodes.GETFIELD, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.GETFIELD, Opcodes.GETSTATIC, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.GETSTATIC, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.GETFIELD, Finder.MULTIPLY},
                {Finder.VARIABLE, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.MULTIPLY, Opcodes.LDC, Finder.ARITHMETIC},
                {Finder.VARIABLE, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.ARITHMETIC},
                {Finder.CONSTANT, Opcodes.GETSTATIC, Opcodes.LDC, Finder.COMPARISON, Finder.MULTIPLY},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Finder.COMPARISON, Finder.MULTIPLY},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Opcodes.LDC, Finder.VARIABLE, Finder.VARIABLE, Finder.ARITHMETIC, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.VARIABLE, Finder.MULTIPLY, Finder.VARIABLE, Finder.ARITHMETIC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.CONSTANT, Finder.ARITHMETIC, Finder.ARITHMETIC, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.CONSTANT, Finder.ARITHMETIC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.VARIABLE, Finder.MULTIPLY, Finder.VARIABLE, Finder.ARITHMETIC, Finder.ARITHMETIC, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.I2D, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DSUB, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.DLOAD, Opcodes.DMUL, Opcodes.DSUB, Opcodes.DMUL},
        };

        for (int[] pattern : patterns) {
            this.multi_count += this.moveInstructions(method, pattern, 0, pattern.length - 1);
        }

        //Swap(LDC, ILOAD);
        patterns = new int[][]{
                {Opcodes.ALOAD, Opcodes.LDC, Finder.VARIABLE, Opcodes.IMUL},
                {Opcodes.ALOAD, Opcodes.LDC, Finder.VARIABLE, Opcodes.LMUL},
        };

        for (int[] pattern : patterns) {
            this.multi_count += this.moveInstructions(method, pattern, 1, pattern.length - 1);
        }
    }

    private void reorderArithmetic(MethodNode method) {
        //Move(pattern[0], pattern[pattern.length - 1]);
        int patterns[][] = new int[][]{
                {Finder.CONSTANT, Finder.VARIABLE, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.GETSTATIC, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.ALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETSTATIC, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETSTATIC, Opcodes.GETFIELD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.ALOAD, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETSTATIC, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETSTATIC, Opcodes.GETFIELD, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETFIELD, Opcodes.GETSTATIC, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Opcodes.AALOAD, Finder.VARIABLE, Opcodes.AALOAD, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Finder.ARITHMETIC, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.ARITHMETIC},
                {Finder.CONSTANT, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.ARITHMETIC},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.VARIABLE, Opcodes.IALOAD, Finder.COMPARISON},
                {Finder.CONSTANT, Finder.VARIABLE, Finder.CONSTANT, Opcodes.IAND, Finder.COMPARISON}, //(0 != (var & const))

                //{Finder.VARIABLE, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ARRAYLENGTH, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.PUTSTATIC},
                {Finder.CONSTANT, Opcodes.PUTFIELD},
        };

        for (int[] pattern : patterns) {
            this.arithmetic_count += this.moveInstructions(method, pattern, 0, pattern.length - 1);
        }

        //Swap(CONSTANT, ILOAD);  || Move(pattern[0], pattern[pattern.length - 2]);
        patterns = new int[][]{
                {Finder.CONSTANT, Finder.VARIABLE, Finder.ARITHMETIC, Opcodes.PUTFIELD},
                {Finder.CONSTANT, Finder.VARIABLE, Finder.ARITHMETIC, Opcodes.PUTSTATIC},
                {Finder.CONSTANT, Opcodes.ALOAD, Finder.WILDCARD, Finder.CONSTANT, Finder.ARITHMETIC, Finder.MULTIPLY, Opcodes.PUTFIELD}
        };

        for (int[] pattern : patterns) {
            this.arithmetic_count += this.moveInstructions(method, pattern, 0, pattern.length - 2);
        }

        // Swap a set of instructions
        int pattern[] = new int[]{Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.VARIABLE, Finder.MULTIPLY, Finder.VARIABLE, Finder.ARITHMETIC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.CONSTANT, Finder.ARITHMETIC, Finder.ARITHMETIC, Opcodes.LDC, Finder.MULTIPLY};
        this.arithmetic_count += this.moveInstructions(method, pattern, 7, 0, 12);

        pattern = new int[]{Opcodes.ALOAD, Opcodes.GETFIELD, Finder.ARITHMETIC, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.VARIABLE, Finder.MULTIPLY};
        this.arithmetic_count += this.moveInstructions(method, pattern, 5, 3);

//        pattern = new int[]{Opcodes.LDC, Finder.VARIABLE, Finder.VARIABLE, Finder.ARITHMETIC, Finder.MULTIPLY, Opcodes.PUTFIELD};
//        this.arithmetic_count += this.moveInstructions(method, pattern, 0, 5);
    }



    private void countMultipliers(MethodNode method) {
        int patterns[][] = new int[][]{
                {Opcodes.LDC, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.ALOAD, Opcodes.GETSTATIC, Opcodes.GETSTATIC, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.GETSTATIC, Opcodes.GETFIELD, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.GETFIELD, Opcodes.GETSTATIC, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.GETSTATIC, Finder.MULTIPLY},
                {Opcodes.LDC, Opcodes.GETFIELD, Finder.MULTIPLY},
                {Finder.VARIABLE, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.MULTIPLY, Opcodes.LDC, Finder.ARITHMETIC},
                {Finder.VARIABLE, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.ARITHMETIC},
                {Finder.CONSTANT, Opcodes.GETSTATIC, Opcodes.LDC, Finder.COMPARISON, Finder.MULTIPLY},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Finder.COMPARISON, Finder.MULTIPLY},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Opcodes.ALOAD, Opcodes.LDC, Finder.VARIABLE, Opcodes.IMUL},
                {Opcodes.ALOAD, Opcodes.LDC, Finder.VARIABLE, Opcodes.LMUL}
        };

        int fixed_patterns[][] = new int[][]{
                {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY},
                {Opcodes.ALOAD, Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY},
                {Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY},
                {Opcodes.GETFIELD, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY},
                {Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY},
                {Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Finder.MULTIPLY, Opcodes.LDC, Finder.VARIABLE, Finder.ARITHMETIC},
                {Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.VARIABLE, Finder.ARITHMETIC},
                {Opcodes.GETSTATIC, Opcodes.LDC, Finder.COMPARISON, Finder.MULTIPLY},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Finder.COMPARISON, Finder.CONSTANT, Finder.MULTIPLY},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY, Finder.CONSTANT, Finder.COMPARISON},
                {Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.CONSTANT, Finder.COMPARISON},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.CONSTANT, Finder.COMPARISON},
                {Opcodes.ALOAD, Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY, Finder.CONSTANT, Finder.COMPARISON},
        };

        for (int[] pattern : patterns) {
            int i = new DeprecatedFinder(method).findPattern(pattern, 0, true);
            while(i != -1) {
                ++multi_total;
                i = new DeprecatedFinder(method).findPattern(pattern, i + 1, true);
            }
        }

        for (int[] pattern : fixed_patterns) {
            int i = new DeprecatedFinder(method).findPattern(pattern, 0, true);
            while(i != -1) {
                ++multi_total;
                i = new DeprecatedFinder(method).findPattern(pattern, i + 1, true);
            }
        }
    }

    public void countArithmetic(MethodNode method) {
        int patterns[][] = new int[][]{
                {Finder.CONSTANT, Finder.VARIABLE, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.GETSTATIC, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.ALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETSTATIC, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETSTATIC, Opcodes.GETFIELD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.ALOAD, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETSTATIC, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETSTATIC, Opcodes.GETFIELD, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETFIELD, Opcodes.GETSTATIC, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Opcodes.ACONST_NULL, Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Opcodes.AALOAD, Finder.VARIABLE, Opcodes.AALOAD, Finder.VARIABLE, Opcodes.AALOAD, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Finder.ARITHMETIC, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.ARITHMETIC},
                {Finder.CONSTANT, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Opcodes.GETFIELD, Finder.VARIABLE, Opcodes.IALOAD, Finder.COMPARISON},
                {Finder.CONSTANT, Finder.VARIABLE, Finder.ARITHMETIC, Opcodes.PUTFIELD},
                {Finder.CONSTANT, Finder.VARIABLE, Finder.ARITHMETIC, Opcodes.PUTSTATIC},
                {Finder.CONSTANT, Finder.VARIABLE, Finder.CONSTANT, Opcodes.IAND, Finder.COMPARISON},
                {Finder.CONSTANT, Opcodes.ALOAD, Finder.WILDCARD, Finder.CONSTANT, Finder.ARITHMETIC, Finder.MULTIPLY, Opcodes.PUTFIELD}
        };

        int fixed_patterns[][] = new int[][]{
                {Finder.VARIABLE, Finder.CONSTANT, Finder.COMPARISON},
                {Opcodes.GETSTATIC, Finder.CONSTANT, Finder.COMPARISON},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Finder.CONSTANT, Finder.COMPARISON},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ACONST_NULL, Finder.COMPARISON},
                {Opcodes.ALOAD, Opcodes.ACONST_NULL, Finder.COMPARISON},
                {Opcodes.GETSTATIC, Opcodes.ACONST_NULL, Finder.COMPARISON},
                {Opcodes.GETSTATIC, Opcodes.GETFIELD, Opcodes.ACONST_NULL, Finder.COMPARISON},
                {Opcodes.ALOAD, Finder.VARIABLE, Opcodes.AALOAD, Opcodes.ACONST_NULL, Finder.COMPARISON},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Finder.VARIABLE, Opcodes.AALOAD, Opcodes.ACONST_NULL, Finder.COMPARISON},
                {Opcodes.GETSTATIC, Finder.VARIABLE, Opcodes.AALOAD, Opcodes.ACONST_NULL, Finder.COMPARISON},
                {Opcodes.GETSTATIC, Opcodes.GETFIELD, Finder.VARIABLE, Opcodes.AALOAD, Opcodes.ACONST_NULL, Finder.COMPARISON},
                {Opcodes.GETFIELD, Opcodes.GETSTATIC, Finder.VARIABLE, Opcodes.AALOAD, Opcodes.ACONST_NULL, Finder.COMPARISON},
                {Opcodes.GETSTATIC, Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Opcodes.AALOAD, Finder.VARIABLE, Opcodes.AALOAD, Finder.VARIABLE, Opcodes.AALOAD, Opcodes.ACONST_NULL, Finder.COMPARISON},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Finder.ARITHMETIC, Finder.CONSTANT, Finder.COMPARISON},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Finder.CONSTANT, Finder.ARITHMETIC},
                {Opcodes.GETSTATIC, Opcodes.LDC, Finder.MULTIPLY, Finder.CONSTANT, Finder.COMPARISON},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Finder.MULTIPLY, Finder.CONSTANT, Finder.COMPARISON},
                {Opcodes.ALOAD, Opcodes.GETFIELD, Finder.VARIABLE, Opcodes.IALOAD, Finder.CONSTANT, Finder.COMPARISON},
                {Finder.VARIABLE, Finder.CONSTANT, Finder.ARITHMETIC, Opcodes.PUTFIELD},
                {Finder.VARIABLE, Finder.CONSTANT, Finder.ARITHMETIC, Opcodes.PUTSTATIC},
                {Finder.VARIABLE, Finder.CONSTANT, Opcodes.IAND, Finder.CONSTANT, Finder.COMPARISON},
                {Opcodes.ALOAD, Finder.WILDCARD, Finder.CONSTANT, Finder.ARITHMETIC, Finder.CONSTANT, Finder.MULTIPLY, Opcodes.PUTFIELD}
        };

        for (int[] pattern : patterns) {
            int i = new DeprecatedFinder(method).findPattern(pattern, 0, true);
            while(i != -1) {
                ++arithmetic_total;
                i = new DeprecatedFinder(method).findPattern(pattern, i + 1, true);
            }
        }

        for (int[] pattern : fixed_patterns) {
            int i = new DeprecatedFinder(method).findPattern(pattern, 0, true);
            while(i != -1) {
                ++arithmetic_total;
                i = new DeprecatedFinder(method).findPattern(pattern, i + 1, true);
            }
        }
    }
}
