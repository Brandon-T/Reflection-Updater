package acid.modifiers.visitors;

import jdk.internal.org.objectweb.asm.ClassVisitor;

/**
 * Created by Kira on 2015-01-15.
 */
public abstract class AbstractMethodVisitor extends AbstractClassVisitor {
    public AbstractMethodVisitor(ClassVisitor visitor, int access, String name, String desc, boolean add) {
        super(visitor, access, name, desc, add);
    }

    public AbstractMethodVisitor(IClassVisitor visitor, int access, String name, String desc, boolean add) {
        super(visitor, access, name, desc, add);
    }

    public AbstractMethodVisitor(IClassVisitor visitor, int access, String name, String desc, String signature, String[] exceptions, boolean add) {
        super(visitor, access, name, desc, signature, exceptions, add);
    }

    @Override
    protected void modify() {
        super.modifyMethod();
    }
}
