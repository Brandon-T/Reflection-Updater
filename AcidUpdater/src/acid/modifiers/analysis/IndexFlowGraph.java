package acid.modifiers.analysis;

import acid.interpreters.IndexValue;
import acid.interpreters.SimpleIndexInterpreter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.analysis.Analyzer;
import jdk.internal.org.objectweb.asm.tree.analysis.AnalyzerException;
import jdk.internal.org.objectweb.asm.tree.analysis.Frame;

/**
 * Created by Kira on 2015-01-20.
 */
public class IndexFlowGraph implements Opcodes {
    String owner;
    MethodNode method;
    private Analyzer<IndexValue> analyser;
    private Frame<IndexValue>[] frames;

    public IndexFlowGraph(String owner, MethodNode method) {
        this.owner = owner;
        this.method = method;

        this.analyser = new Analyzer<IndexValue>(new SimpleIndexInterpreter(method)) {
            protected Frame<IndexValue> newFrame(int nLocals, int nStack) {
                return new Node<>(nLocals, nStack);
            }

            protected Frame<IndexValue> newFrame(Frame<? extends IndexValue> src) {
                return new Node<>(src);
            }

            protected void newControlFlowEdge(int src, int dst) {
                Node<IndexValue> s = (Node<IndexValue>) getFrames()[src];
                s.successors.add((Node<IndexValue>) getFrames()[dst]);
            }
        };
    }

    public IndexFlowGraph analyse() {
        try {
            frames = analyser.analyze(owner, method);
            return this;
        } catch (AnalyzerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Frame<IndexValue>[] getFrames() {
        return this.frames;
    }
}
