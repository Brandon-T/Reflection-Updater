package org.acid.updater.modifiers.analysis;

import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

/**
 * Created by Kira on 2015-01-22.
 */
public class StackAnalyser {




    public <V extends Value> V getStackValue(Frame<V> f, int index) {
        int top = f.getStackSize() - 1;
        return index <= top ? f.getStack(top - index) : null;
    }
}
