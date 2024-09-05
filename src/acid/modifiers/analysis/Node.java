package acid.modifiers.analysis;

import jdk.internal.org.objectweb.asm.tree.analysis.Frame;
import jdk.internal.org.objectweb.asm.tree.analysis.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kira on 2015-01-16.
 */
public class Node<V extends Value> extends Frame<V> {
    public Set<Node<V>> successors = new HashSet<>();

    public Node(Frame<? extends V> src) {
        super(src);
    }

    public Node(int nLocals, int nStack) {
        super(nLocals, nStack);
    }
}