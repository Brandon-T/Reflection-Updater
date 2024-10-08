package org.acid.updater.modifiers.analysis;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

/**
 * Created by Brandon on 2015-01-13.
 */
public class BasicFlowGraph implements Opcodes {
    String owner;
    MethodNode method;
    private final Analyzer<BasicValue> analyser;
    private Frame<BasicValue>[] frames;

    public BasicFlowGraph(String owner, MethodNode method) {
        this.owner = owner;
        this.method = method;

        this.analyser = new Analyzer<BasicValue>(new BasicInterpreter()) {
            protected Frame<BasicValue> newFrame(int nLocals, int nStack) {
                if (nStack == 0) {
                    return new Node<>(nLocals, 1);
                }
                return new Node<>(nLocals, nStack);
            }

            protected Frame<BasicValue> newFrame(Frame<? extends BasicValue> src) {
                return new Node<>(src);
            }

            protected void newControlFlowEdge(int src, int dst) {
                Node<BasicValue> s = (Node<BasicValue>) getFrames()[src];
                s.successors.add((Node<BasicValue>) getFrames()[dst]);
            }
        };
    }

    public BasicFlowGraph analyse() {
        try {
            analyser.analyze(owner, method);
            frames = analyser.getFrames();
            return this;
        } catch (AnalyzerException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Frame<BasicValue>[] getFrames() {
        return this.frames;
    }

    public BasicValue getStackValue(Frame<BasicValue> f, int index) {
        int top = f.getStackSize() - 1;
        return index <= top ? f.getStack(top - index) : null;
    }
}