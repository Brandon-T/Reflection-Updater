package org.acid.updater.modifiers.adapters;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

/**
 * Created by Kira on 2015-01-15.
 */
public class IDynamicClassAdapter extends IClassAdapter {
    private ClassVisitor nextVisitor;

    public IDynamicClassAdapter(ClassVisitor visitor) {
        super(visitor);
        this.nextVisitor = visitor;
    }

    @Override
    protected void apply(ClassVisitor visitor) {
        super.inlineCopy((ClassNode)nextVisitor, (ClassNode)visitor);
    }
}
