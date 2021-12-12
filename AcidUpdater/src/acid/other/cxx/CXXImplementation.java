package acid.other.cxx;

import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class CXXImplementation {
    CXXHeader header;

    public CXXImplementation(CXXHeader header) {
        this.header = header;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        header.getPublicMethods().forEach((m) -> {
            builder.append(m.getImplementationSignature(header.getClassName())).append("\n");
            builder.append("{\n");

            builder.append(this.generateMethodBody(m));

            builder.append("}\n\n");
        });

        header.getProtectedMethods().forEach((m) -> {
            builder.append(m.getImplementationSignature(header.getClassName())).append("\n");
            builder.append("{\n");

            builder.append(this.generateMethodBody(m));

            builder.append("}\n\n");
        });

        return builder.toString();
    }

    private String generateMethodBody(CXXMethod method) {

        MethodNode node = method.getMethodNode();
        StringBuilder builder = new StringBuilder();
        builder.append("    ");
        builder.append("static jmethodID").append(" ");
        builder.append(node.name).append("Method").append(" = ");

        if (method.isStatic()) {
            builder.append("this->vm->GetStaticMethodID(this->cls.get(), ");
        }
        else {
            builder.append("this->vm->GetMethodID(this->cls.get(), ");
        }

        builder.append('"').append(node.name).append("Method").append('"').append(", ");
        builder.append('"').append(node.desc).append('"').append(");");
        builder.append("\n");

        builder.append(this.generateReturnType(method));

        builder.append("\n");
        return builder.toString();
    }

    private String generateReturnType(CXXMethod method) {

        MethodNode node = method.getMethodNode();
        Type returnType = Type.getReturnType(method.getMethodNode().desc);
        CXXArgument[] arguments = CXXArgument.generateArguments(node);

        StringBuilder builder = new StringBuilder();
        builder.append("    ");

        if (returnType.getSort() != Type.VOID && returnType.getSort() != Type.ARRAY) {
            builder.append("return").append(" ");
        }

        switch (returnType.getSort()) {
            case Type.VOID: {
                builder.append("(this->vm, this->vm->CallVoidMethod(this->inst.get(), ");
                builder.append(node.name).append("Method").append("));");
            }
            break;

            case Type.BOOLEAN: {
                builder.append("this->vm->CallBooleanMethod(this->inst.get(), ");
                builder.append(node.name).append("Method");
                builder.append(arguments.length > 0 ? ", " : "");
                builder.append(this.generateReturnTypeArguments(arguments));
                builder.append(");");
            }
            break;

            case Type.CHAR: {
                builder.append("this->vm->CallCharMethod(this->inst.get(), ");
                builder.append(node.name).append("Method");
                builder.append(arguments.length > 0 ? ", " : "");
                builder.append(this.generateReturnTypeArguments(arguments));
                builder.append(");");
            }
            break;

            case Type.BYTE: {
                builder.append("this->vm->CallByteMethod(this->inst.get(), ");
                builder.append(node.name).append("Method");
                builder.append(arguments.length > 0 ? ", " : "");
                builder.append(this.generateReturnTypeArguments(arguments));
                builder.append(");");
            }
            break;

            case Type.SHORT: {
                builder.append("this->vm->CallShortMethod(this->inst.get(), ");
                builder.append(node.name).append("Method");
                builder.append(arguments.length > 0 ? ", " : "");
                builder.append(this.generateReturnTypeArguments(arguments));
                builder.append(");");
            }
            break;

            case Type.INT: {
                builder.append("this->vm->CallIntMethod(this->inst.get(), ");
                builder.append(node.name).append("Method");
                builder.append(arguments.length > 0 ? ", " : "");
                builder.append(this.generateReturnTypeArguments(arguments));
                builder.append(");");
            }
            break;

            case Type.FLOAT: {
                builder.append("this->vm->CallFloatMethod(this->inst.get(), ");
                builder.append(node.name).append("Method");
                builder.append(arguments.length > 0 ? ", " : "");
                builder.append(this.generateReturnTypeArguments(arguments));
                builder.append(");");
            }
            break;

            case Type.LONG: {
                builder.append("this->vm->CallLongMethod(this->inst.get(), ");
                builder.append(node.name).append("Method");
                builder.append(arguments.length > 0 ? ", " : "");
                builder.append(this.generateReturnTypeArguments(arguments));
                builder.append(");");
            }
            break;

            case Type.DOUBLE: {
                builder.append("this->vm->CallDoubleMethod(this->inst.get(), ");
                builder.append(node.name).append("Method");
                builder.append(arguments.length > 0 ? ", " : "");
                builder.append(this.generateReturnTypeArguments(arguments));
                builder.append(");");
            }
            break;

            case Type.ARRAY: {
                builder.append("static jarray arr = reinterpret_cast<jarray>(");
                builder.append("this->vm->CallObjectMethod(this->inst.get(), ");
                builder.append(node.name).append("Method");
                builder.append(arguments.length > 0 ? ", " : "");
                builder.append(this.generateReturnTypeArguments(arguments));
                builder.append("));").append("\n");
                builder.append("    ").append("return").append(" ");
                builder.append("Array<").append(Type.getReturnType(node.desc).getElementType().getClassName()).append(">");
                builder.append("(this->vm, arr);");
            }
            break;

            case Type.OBJECT: {
                builder.append(CXXUtilities.getNormalizedReturnType(node));
                builder.append("(this->vm, this->vm->CallObjectMethod(this->inst.get(), ");
                builder.append(node.name).append("Method");
                builder.append(arguments.length > 0 ? ", " : "");
                builder.append(this.generateReturnTypeArguments(arguments));
                builder.append("));");
            }
            break;

            case Type.METHOD: {

            }
            break;
        }

        return builder.toString();
    }

    private String generateReturnTypeArguments(CXXArgument[] args) {
        String[] argStrings = new String[args.length];
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i].getName() + args[i].getIndex();

            switch (args[i].getType().getSort()) {
                case Type.ARRAY: {
                    arg += ".ref().get()";
                }
                break;

                case Type.OBJECT: {
                    arg += ".ref().get()";
                }
                break;

                case Type.METHOD: {

                }
            }

            argStrings[i] = arg;
        }
        return String.join(", ", argStrings);
    }
}
