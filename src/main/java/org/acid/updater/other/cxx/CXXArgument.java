package org.acid.updater.other.cxx;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

public class CXXArgument {
    private final Type type;
    private final String normalizedType;
    private final String name;
    private final int index;

    public CXXArgument(Type type, String name, int index) {
        this.type = type;
        this.normalizedType = CXXUtilities.normalizeType(type);
        this.name = name;
        this.index = index;
    }

    public static CXXArgument[] generateArguments(MethodNode methodNode) {
        Type[] methodArgs = Type.getArgumentTypes(methodNode.desc);
        CXXArgument[] args = new CXXArgument[methodArgs.length];

        for (int i = 0; i < methodArgs.length; ++i) {
            args[i] = new CXXArgument(methodArgs[i], "arg", i);
        }
        return args;
    }

    public Type getType() {
        return type;
    }

    public String getNormalizedType() {
        return normalizedType;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
}