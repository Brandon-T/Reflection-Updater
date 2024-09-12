package org.acid.updater.modifiers.adapters;

import org.objectweb.asm.ClassVisitor;

/**
 * Created by Brandon on 2015-01-15.
 */
public abstract class AbstractMethodAdapter extends AbstractClassAdapter {
    public AbstractMethodAdapter(ClassVisitor visitor, int access, String name, String desc, boolean add) {
        super(visitor, access, name, desc, add);
    }

    public AbstractMethodAdapter(IClassAdapter adapter, int access, String name, String desc, boolean add) {
        super(adapter, access, name, desc, add);
    }

    public AbstractMethodAdapter(IClassAdapter adapter, int access, String name, String desc, String signature, String[] exceptions, boolean add) {
        super(adapter, access, name, desc, signature, exceptions, add);
    }

    @Override
    protected void modify() {
        super.modifyMethod();
    }
}
