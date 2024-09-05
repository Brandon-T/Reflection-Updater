package acid.modifiers.adapters;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

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
