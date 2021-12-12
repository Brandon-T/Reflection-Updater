package acid.modifiers.adapters;

import jdk.internal.org.objectweb.asm.ClassVisitor;

/**
 * Created by Kira on 2015-01-15.
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
