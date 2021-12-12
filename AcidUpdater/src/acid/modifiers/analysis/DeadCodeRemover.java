package acid.modifiers.analysis;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.analysis.Frame;

/**
 * Created by Kira on 2015-01-16.
 */
public class DeadCodeRemover {
    private BasicFlowGraph graph;
    private MethodNode method;

    public DeadCodeRemover(String owner, MethodNode method) {
        this.graph = new BasicFlowGraph(owner, this.method = method);
    }

    public DeadCodeRemover analyse() {
        graph.analyse();
        return this;
    }

    public void remove() {
        Frame[] frames = graph.getFrames();
        AbstractInsnNode[] instructions = method.instructions.toArray();
        for (int i = 0; i < frames.length; ++i) {
            AbstractInsnNode instruction = instructions[i];
            if (frames[i] == null && !(instruction instanceof LabelNode)) {
                method.instructions.remove(instructions[i]);
                System.out.println("Removed Dead Code");
            }
        }
    }
}
