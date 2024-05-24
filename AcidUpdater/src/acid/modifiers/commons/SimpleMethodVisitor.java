package acid.modifiers.commons;

import acid.modifiers.visitors.AbstractMethodVisitor;
import acid.modifiers.visitors.IClassVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

/**
 * Created by Kira on 2015-01-15.
 */
public class SimpleMethodVisitor extends AbstractMethodVisitor {
    Consumer<MethodVisitor> body;

    public SimpleMethodVisitor(ClassVisitor visitor, int access, String name, String desc, boolean add, Consumer<MethodVisitor> body) {
        super(visitor, access, name, desc, add);
        this.body = body;
    }

    public SimpleMethodVisitor(IClassVisitor visitor, int access, String name, String desc, boolean add, Consumer<MethodVisitor> body) {
        super(visitor, access, name, desc, add);
        this.body = body;
    }

    public SimpleMethodVisitor(IClassVisitor visitor, int access, String name, String desc, String signature, String[] exceptions, boolean add, Consumer<MethodVisitor> body) {
        super(visitor, access, name, desc, signature, exceptions, add);
        this.body = body;
    }

    @Override
    protected void methodBody(MethodVisitor mv) {
        if (body != null) {
            body.accept(mv);
        }
    }
}
