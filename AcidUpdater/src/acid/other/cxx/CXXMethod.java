package acid.other.cxx;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class CXXMethod {
    private boolean hasPublicAccess;
    private boolean hasProtectedAccess;
    private boolean hasStaticAccess;
    private MethodNode methodNode;

    public CXXMethod(MethodNode methodNode) {
        this.methodNode = methodNode;
        this.hasPublicAccess = (methodNode.access & Opcodes.ACC_PUBLIC) != 0;
        this.hasProtectedAccess = (methodNode.access & Opcodes.ACC_PROTECTED) != 0;
        this.hasStaticAccess = (methodNode.access & Opcodes.ACC_STATIC) != 0;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public boolean isPublic() {
        return this.hasPublicAccess;
    }

    public boolean isProtected() {
        return this.hasProtectedAccess;
    }

    public boolean isStatic() {
        return this.hasStaticAccess;
    }

    public String getName() {
        return methodNode.name;
    }

    public String getHeaderSignature() {
        StringBuilder builder = new StringBuilder();
        if (this.isStatic()) {
            builder.append("static ");
        }
        builder.append(this.generateReturnType()).append(" ").append(methodNode.name);
        builder.append("(");
        builder.append(this.generateArguments());
        builder.append(");");
        return builder.toString();
    }

    public String getImplementationSignature(String className) {
        StringBuilder builder = new StringBuilder();
        builder.append(this.generateReturnType()).append(" ");
        builder.append(className).append("::").append(methodNode.name);
        builder.append("(").append(this.generateArguments()).append(")");
        return builder.toString();
    }

    private String generateReturnType() {
        Type returnType = Type.getReturnType(methodNode.desc);

        if (returnType.getSort() == Type.ARRAY) {
            String result = "Array<";
            result += Type.getReturnType(methodNode.desc).getElementType().getClassName();
            result += ">";
            return result;
        }

        return CXXUtilities.getNormalizedReturnType(methodNode);
    }

    private String generateArguments() {
        CXXArgument[] args = CXXArgument.generateArguments(methodNode);
        String[] argStrings = new String[args.length];
        for (int i = 0; i < args.length; ++i) {
            if (args[i].getType().getSort() == Type.ARRAY) {
                String elementType = args[i].getType().getElementType().getClassName();
                if (elementType != null) {
                    argStrings[i] = "Array<" + elementType + ">& ";
                }
                else {
                    argStrings[i] = "Array<" + CXXUtilities.normalizeType(args[i].getType()) + ">& ";
                }

                argStrings[i] = argStrings[i] + args[i].getName() + args[i].getIndex();
            }
            else {
                argStrings[i] = CXXUtilities.normalizeType(args[i].getType()) + " " + args[i].getName() + args[i].getIndex();
            }
        }
        return String.join(", ", argStrings);
    }
}