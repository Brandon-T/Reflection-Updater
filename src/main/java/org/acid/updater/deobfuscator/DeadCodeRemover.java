package org.acid.updater.deobfuscator;

import org.acid.updater.modifiers.analysis.BasicFlowGraph;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.Collection;
import java.util.HashMap;

public class DeadCodeRemover extends Deobfuscator {
    private int totalInstructions;
    private final HashMap<MethodNode, BasicFlowGraph> graphs;

    public DeadCodeRemover(Collection<ClassNode> classes) {
        super(classes);

        this.graphs = new HashMap<>();
    }

    @Override
    public Deobfuscator analyse() {
        this.classes.forEach(c -> c.methods.forEach(m -> {
            this.graphs.put(m, new BasicFlowGraph(c.name, m).analyse());
            this.totalInstructions += m.instructions.size();
        }));
        return this;
    }

    @Override
    public void remove() {
        int instructionsRemoved = 0;

        for (MethodNode m : this.graphs.keySet()) {
            BasicFlowGraph graph = this.graphs.get(m);
            Frame[] frames = graph.getFrames();
            AbstractInsnNode[] instructions = m.instructions.toArray();
            for (int i = 0; i < frames.length; ++i) {
                AbstractInsnNode instruction = instructions[i];
                if (frames[i] == null && !(instruction instanceof LabelNode)) {
                    m.instructions.remove(instructions[i]);
                    ++instructionsRemoved;
                }
            }
        }

        System.out.println("Removed DeadCode Instructions: " + instructionsRemoved + " of " + totalInstructions);
    }
}
