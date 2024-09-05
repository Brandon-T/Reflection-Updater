package org.acid.updater.modifiers.analysis;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

/**
 * Created by Kira on 2015-01-16.
 */
public class SourceFlowGraph implements Opcodes {
    String owner;
    MethodNode method;
    private Analyzer<SourceValue> analyser;
    private Frame<SourceValue>[] frames;

    public SourceFlowGraph(String owner, MethodNode method) {
        this.owner = owner;
        this.method = method;

        this.analyser = new Analyzer<SourceValue>(new SourceInterpreter()) {
            protected Frame<SourceValue> newFrame(int nLocals, int nStack) {
                return new Node<>(nLocals, nStack);
            }

            protected Frame<SourceValue> newFrame(Frame<? extends SourceValue> src) {
                return new Node<>(src);
            }

            protected void newControlFlowEdge(int src, int dst) {
                Node<SourceValue> s = (Node<SourceValue>) getFrames()[src];
                s.successors.add((Node<SourceValue>) getFrames()[dst]);
            }
        };
    }

    public SourceFlowGraph analyse() {
        try {
            analyser.analyze(owner, method);
            frames = analyser.getFrames();
            return this;
        } catch (AnalyzerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Frame<SourceValue>[] getFrames() {
        return this.frames;
    }

    public SourceValue getStackValue(Frame<SourceValue> f, int index) {
        int top = f.getStackSize() - 1;
        return index <= top ? f.getStack(top - index) : null;
    }

    public int getFirstStackIndex(Frame<SourceValue> frame, MethodNode method, String desc) {
        SourceValue v = frame.getStack(frame.getStackSize() - Type.getArgumentTypes(desc).length - 1);
        AbstractInsnNode[] instructions = v.insns.toArray(new AbstractInsnNode[1]);
        return method.instructions.indexOf(instructions[0]);
    }

    public int getLastStackIndex(Frame<SourceValue> frame, MethodNode method) {
        SourceValue v = frame.getStack(frame.getStackSize() - 1);
        AbstractInsnNode[] instructions = v.insns.toArray(new AbstractInsnNode[1]);
        return method.instructions.indexOf(instructions[0]);
    }

    public int getLastArgument(Frame<SourceValue>[] frames, MethodNode method, int index) {
        if (index != -1) {
            MethodInsnNode called = (MethodInsnNode) method.instructions.get(index);
            index = getLastStackIndex(frames[index], method);
            if (method.instructions.get(index) instanceof MethodInsnNode) {
                MethodInsnNode mi = (MethodInsnNode) method.instructions.get(index);
                index = method.instructions.indexOf(mi);
                return getFirstStackIndex(frames[index], method, mi.desc) - ((mi.getOpcode() == Opcodes.INVOKESTATIC) ? 0 : 1);
            }

            if (!isNumeric(method.instructions.get(index).getOpcode())) {
                Type[] arg_types = Type.getArgumentTypes(called.desc);
                return getFirstStackIndex(frames[index], method, called.desc) + (isConversion(method.instructions.get(index).getOpcode()) ? arg_types[arg_types.length - 1].getSize() - 1 : 0);
            }
            return index;
        }
        return -1;
    }

    private boolean isNumeric(int opcode) {
        return (opcode >= Opcodes.ACONST_NULL && opcode <= Opcodes.DLOAD);
    }

    private boolean isConversion(int opcode) {
        return (opcode >= Opcodes.I2L && opcode <= Opcodes.I2S);
    }
}
