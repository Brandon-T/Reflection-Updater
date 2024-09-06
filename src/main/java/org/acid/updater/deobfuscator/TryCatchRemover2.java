package org.acid.updater.deobfuscator;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public class TryCatchRemover2 extends Deobfuscator {
    private int try_count = 0, removal_count = 0;

    public TryCatchRemover2(Collection<ClassNode> classes) {
        super(classes);
    }

    public TryCatchRemover2 analyse() {
        classes.stream().forEach(n -> n.methods.forEach(m -> {
            if (m.tryCatchBlocks != null) {
                try_count += m.tryCatchBlocks.size();
            }
        }));
        return this;
    }

    public void remove() {
        classes.stream().forEach(n -> n.methods.forEach(this::removeTryCatch));
        System.out.println("Removed TryCatchBlocks (Aggressively): " + removal_count + " of " + try_count);
    }

    private void removeTryCatch(MethodNode method) {
        if (method.tryCatchBlocks != null) {
            removal_count += method.tryCatchBlocks.size();

            MethodNode m = new MethodNode();
            method.accept(new TryCatchRemoverVisitor(m));
            method.instructions.clear();
            method.instructions.add(m.instructions);
            method.tryCatchBlocks.clear();
        }
    }

    private class TryCatchRemoverVisitor extends MethodVisitor {
        public TryCatchRemoverVisitor(final MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            //mv.visitTryCatchBlock(start, end, handler, type); //Comment out to remove try-catch blocks
        }
    }

}
