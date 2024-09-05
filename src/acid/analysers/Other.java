package acid.analysers;

import acid.structures.ClassInfo;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.Collection;

/**
 * Created by Kira on 2014-12-10.
 */
public class Other extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        return null;
    }
}
