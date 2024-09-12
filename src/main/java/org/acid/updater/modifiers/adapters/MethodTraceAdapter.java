package org.acid.updater.modifiers.adapters;

import org.objectweb.asm.*;

public class MethodTraceAdapter extends ClassVisitor {
    private final String owner;
    private final String method;
    private final String desc;

    public MethodTraceAdapter(final ClassVisitor cv, String owner, String method, String desc) {
        super(Opcodes.ASM5, cv);

        this.owner = owner;
        this.method = method;
        this.desc = desc;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        return mv == null ? null : new MethodTraceVisitor(mv, this.owner, this.method, this.desc, access, name, desc);
    }

    private class MethodTraceVisitor extends MethodVisitor {
        private final String owner;
        private final String method;
        private final String desc;

        private final int access;
        private final String name;
        private final String descriptor;

        public MethodTraceVisitor(final MethodVisitor mv, String owner, String method, String desc,
                                  final int access, final String name, final String descriptor) {
            super(Opcodes.ASM5, mv);

            this.owner = owner;
            this.method = method;
            this.desc = desc;

            this.access = access;
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            /*if (this.owner != null && this.method != null && this.desc != null) {
                if (owner.equals(this.owner) && name.equals(this.method) && desc.equals(this.desc)) {
                    mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                    mv.visitLdcInsn(owner + "." + name + " -> " + desc);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                }
            }
            else if (this.owner != null && this.method != null) {
                if (owner.equals(this.owner) && name.equals(this.method)) {
                    mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                    mv.visitLdcInsn(owner + "." + name + " -> " + desc);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                }
            }
            else if (this.method != null && this.desc != null) {
                if (name.equals(this.method) && desc.equals(this.desc)) {
                    mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                    mv.visitLdcInsn(owner + "." + name + " -> " + desc);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                }
            }
            else if (this.owner != null && this.desc != null) {
                if (owner.equals(this.owner) && desc.equals(this.desc)) {
                    mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                    mv.visitLdcInsn(owner + "." + name + " -> " + desc);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                }
            }
            else if (this.owner == null && this.method == null && this.desc == null) {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                mv.visitLdcInsn(owner + "." + name + " -> " + desc);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            }*/

            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            /*if (this.owner == null && this.method == null && this.desc == null) {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                mv.visitLdcInsn(owner + "." + name + " -> " + descriptor);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            }*/

            mv.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            mv.visitTryCatchBlock(start, end, handler, type); //Comment out to remove try-catch blocks
        }

        @Override
        public void visitCode() {
            if (this.name.equals("<clinit>")) {
                super.visitCode();
                return;
            }

            int offset = ((this.access & Opcodes.ACC_STATIC) != 0) ? 0 : 1;

            int argumentCount = (Type.getArgumentsAndReturnSizes(this.descriptor) >> 2) - 1;
            Type[] types = Type.getArgumentTypes(this.descriptor);

            mv.visitIntInsn(Opcodes.BIPUSH, argumentCount); //BIPUSH count
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object"); //Create new array


            for (int i = 0; i < argumentCount; ++i) {
                Type type = types[i];
                int opcode = getStackOperand(type);

                mv.visitInsn(Opcodes.DUP);
                mv.visitIntInsn(Opcodes.BIPUSH, i);  //Load array index
                mv.visitVarInsn(opcode, i + offset); ////mv.visitIntInsn(opcode, i + offset); //Store each parameter
                this.invoke(type); //Stringify parameter if needed
                mv.visitInsn(Opcodes.AASTORE); //Store in the array
                mv.visitInsn(Opcodes.DUP);
            }

            mv.visitVarInsn(Opcodes.ASTORE, argumentCount + offset); //Store array on the stack

            //PRINTING
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
            mv.visitLdcInsn(this.owner + "." + this.name + "(");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);

            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
            mv.visitVarInsn(Opcodes.ALOAD, argumentCount + offset); //Load array..
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Arrays", "deepToString", "([Ljava/lang/Object;)Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);

            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
            mv.visitLdcInsn(")");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

            mv.visitCode();
        }

        private int getStackOperand(Type type) {
            switch (type.getSort()) {
                case Type.BOOLEAN:
                    return Opcodes.ILOAD;

                case Type.CHAR:
                    return Opcodes.ILOAD;

                case Type.BYTE:
                    return Opcodes.ILOAD;

                case Type.SHORT:
                    return Opcodes.ILOAD;

                case Type.INT:
                    return Opcodes.ILOAD;

                case Type.FLOAT:
                    return Opcodes.FLOAD;

                case Type.LONG:
                    return Opcodes.LLOAD;

                case Type.DOUBLE:
                    return Opcodes.DLOAD;

                case Type.ARRAY:
                    return Opcodes.ALOAD;

                case Type.OBJECT:
                    return Opcodes.ALOAD;

                case Type.METHOD:
                    return Opcodes.ALOAD;
            }
            return Opcodes.ALOAD;
        }

        private void invoke(Type type) {
            switch (type.getSort()) {
                case Type.BOOLEAN:
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                    break;

                case Type.CHAR:
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                    break;

                case Type.BYTE:
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                    break;

                case Type.SHORT:
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                    break;

                case Type.INT:
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                    break;

                case Type.FLOAT:
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                    break;

                case Type.LONG:
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                    break;

                case Type.DOUBLE:
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                    break;

                case Type.ARRAY:
                    break;

                case Type.OBJECT:
                    break;

                case Type.METHOD:
                    break;
            }
        }
    }
}
