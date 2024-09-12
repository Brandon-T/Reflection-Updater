package org.acid.updater.modifiers.delegators;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

/**
 * Created by Brandon on 2015-01-15.
 */
public class IDynamicClassDelegator extends IClassDelegator {
    private final ClassVisitor nextVisitor;

    public IDynamicClassDelegator(ClassVisitor visitor) {
        super(new ClassNode());
        this.nextVisitor = visitor;
    }

    @Override
    protected void apply(ClassVisitor visitor) {
        super.inlineCopy((ClassNode) nextVisitor, (ClassNode) visitor);
    }
}
