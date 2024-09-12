package org.acid.updater.modifiers.visitors;

import org.objectweb.asm.ClassVisitor;

/**
 * Created by Brandon on 2015-01-15.
 */
public class IStaticClassVisitor extends IClassVisitor {
    public IStaticClassVisitor(ClassVisitor visitor) {
        super(visitor);
    }

    @Override
    protected void apply(ClassVisitor visitor) {
    }
}
