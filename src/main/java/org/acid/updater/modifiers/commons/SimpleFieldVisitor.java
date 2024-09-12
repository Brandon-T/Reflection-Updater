package org.acid.updater.modifiers.commons;

import org.acid.updater.modifiers.visitors.AbstractClassVisitor;
import org.acid.updater.modifiers.visitors.IClassVisitor;
import org.objectweb.asm.ClassVisitor;

/**
 * Created by Brandon on 2015-01-15.
 */
public class SimpleFieldVisitor extends AbstractClassVisitor {
    public SimpleFieldVisitor(ClassVisitor visitor, int access, String name, String desc, boolean add) {
        super(visitor, access, name, desc, add);
    }

    public SimpleFieldVisitor(IClassVisitor visitor, int access, String name, String desc, boolean add) {
        super(visitor, access, name, desc, add);
    }

    public SimpleFieldVisitor(IClassVisitor visitor, int access, String name, String desc, String signature, String[] exceptions, boolean add) {
        super(visitor, access, name, desc, signature, exceptions, add);
    }

    @Override
    protected void modify() {
        super.modifyField();
    }
}
