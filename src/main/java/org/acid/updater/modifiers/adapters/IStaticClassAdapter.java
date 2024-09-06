package org.acid.updater.modifiers.adapters;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

/**
 * Created by Kira on 2015-01-15.
 */
public class IStaticClassAdapter extends IClassAdapter {
    public IStaticClassAdapter(ClassVisitor visitor) {
        super(visitor);
    }

    @Override
    protected void apply(ClassVisitor visitor) {
        ((ClassNode)visitor).accept(super.getVisitor());
    }
}
