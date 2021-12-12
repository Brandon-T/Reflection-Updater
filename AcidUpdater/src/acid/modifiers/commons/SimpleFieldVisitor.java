package acid.modifiers.commons;

import acid.modifiers.visitors.AbstractClassVisitor;
import acid.modifiers.visitors.IClassVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;

/**
 * Created by Kira on 2015-01-15.
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
