package org.acid.updater.interpreters;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Value;

/**
 * Created by Brandon on 2015-01-19.
 */
public class IndexValue implements Value {
    private final Type type;
    private final int index;


    public IndexValue(Type type) {
        this(type, -1);
    }

    public IndexValue(final Type type, final int index) {
        this.type = type;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int getSize() {
        return this.type != Type.LONG_TYPE && this.type != Type.DOUBLE_TYPE ? 1 : 2;
    }

    @Override
    public boolean equals(final Object value) {
        if (value == this) {
            return true;
        }

        if (value instanceof IndexValue) {
            if (this.type == null) {
                return ((IndexValue) value).type == null;
            }
            return this.type.equals(((IndexValue) value).type);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.type == null ? 0 : this.type.hashCode();
    }
}
