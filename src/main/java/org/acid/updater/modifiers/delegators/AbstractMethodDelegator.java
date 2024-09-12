package org.acid.updater.modifiers.delegators;

import org.objectweb.asm.ClassVisitor;

/**
 * Created by Brandon on 2015-01-15.
 */
public class AbstractMethodDelegator extends AbstractClassDelegator {


    public AbstractMethodDelegator(ClassVisitor visitor, int access, String name, String desc, boolean add) {
        super(visitor, access, name, desc, add);
    }

    public AbstractMethodDelegator(IClassDelegator delegator, int access, String name, String desc, boolean add) {
        super(delegator, access, name, desc, add);
    }

    public AbstractMethodDelegator(IClassDelegator delegator, int access, String name, String desc, String signature, String[] exceptions, boolean add) {
        super(delegator, access, name, desc, signature, exceptions, add);
    }

    @Override
    protected void modify() {
        super.modifyMethod();
    }
}
