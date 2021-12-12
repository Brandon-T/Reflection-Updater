package acid.modifiers.visitors;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

/**
 * Created by Kira on 2015-01-15.
 */
public class IDynamicClassVisitor extends IClassVisitor {
    private ClassVisitor nextVisitor;

    public IDynamicClassVisitor(ClassVisitor visitor) {
        super(new ClassNode());
        this.nextVisitor = visitor;
    }

    @Override
    protected void apply(ClassVisitor visitor) {
        super.inlineCopy((ClassNode)nextVisitor, (ClassNode)visitor);
    }
}
