package org.acid.updater.interpreters;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

import java.util.List;

/**
 * Created by Brandon on 2015-01-20.
 */
public class SimpleIndexInterpreter extends Interpreter<IndexValue> implements Opcodes {
    private final InsnList instructions;

    public SimpleIndexInterpreter(MethodNode method) {
        super(Opcodes.ASM5);
        this.instructions = method.instructions;
    }

    @Override
    public IndexValue newValue(Type type) {
        return newValue(type, -1);
    }

    public IndexValue newValue(Type type, int index) {
        if (type == null) {
            return new IndexValue(null, index);
        }

        if (type.getSort() == Type.VOID) {
            return null;
        }
        return new IndexValue(type, index);
    }

    @Override
    public IndexValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
        return new IndexValue(null, instructions.indexOf(insn));
    }

    @Override
    public IndexValue copyOperation(AbstractInsnNode insn, IndexValue indexValue) throws AnalyzerException {
        return new IndexValue(null, instructions.indexOf(insn));
    }

    @Override
    public IndexValue unaryOperation(AbstractInsnNode insn, IndexValue indexValue) throws AnalyzerException {
        return new IndexValue(null, instructions.indexOf(insn));
    }

    @Override
    public IndexValue binaryOperation(AbstractInsnNode insn, IndexValue indexValue, IndexValue v1) throws AnalyzerException {
        return new IndexValue(null, instructions.indexOf(insn));
    }

    @Override
    public IndexValue ternaryOperation(AbstractInsnNode insn, IndexValue indexValue, IndexValue v1, IndexValue v2) throws AnalyzerException {
        return null;//new IndexValue(null, instructions.indexOf(insn));
    }

    @Override
    public IndexValue naryOperation(AbstractInsnNode insn, List<? extends IndexValue> list) throws AnalyzerException {
        return new IndexValue(null, instructions.indexOf(insn));
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, IndexValue indexValue, IndexValue v1) throws AnalyzerException {
    }

    @Override
    public IndexValue merge(IndexValue indexValue, IndexValue v1) {
        return !indexValue.equals(v1) ? new IndexValue(null) : indexValue;
    }
}
