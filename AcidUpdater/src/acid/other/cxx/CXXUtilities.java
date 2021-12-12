package acid.other.cxx;

import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class CXXUtilities {
    public static String normalizeTypeDesc(String string) {
        if (string.contains(".")) {
            int index = string.lastIndexOf(".");
            string = string.substring(index + 1);
        }

        if (string.contains("/")) {
            int index = string.lastIndexOf("/");
            string = string.substring(index + 1);
        }
        return string;
    }

    public static String normalizeType(Type type) {
        String name = type.getClassName();
        if (name.contains(".")) {
            int index = name.lastIndexOf(".");
            name = name.substring(index + 1);
        }

        if (name.contains("/")) {
            int index = name.lastIndexOf("/");
            name = name.substring(index + 1);
        }
        return name;
    }

    public static String getNormalizedArguments(MethodNode methodNode) {
        String args = "";
        Type[] methodArgs = Type.getArgumentTypes(methodNode.desc);

        if (methodArgs.length > 0) {
            for (int i = 0; i < methodArgs.length - 1; ++i) {
                args +=  normalizeType(methodArgs[i]) + " arg" + i + ", ";
            }

            args += normalizeType(methodArgs[methodArgs.length - 1]) + " arg" + (methodArgs.length - 1);
        }
        return args;
    }

    public static String getNormalizedReturnType(MethodNode methodNode) {
        return normalizeType(Type.getReturnType(methodNode.desc));
    }
}
