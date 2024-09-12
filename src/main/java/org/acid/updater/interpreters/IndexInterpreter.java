package org.acid.updater.interpreters;

/**
 * Created by Brandon on 2015-01-20.
 */

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

import java.util.List;

/**
 * Created by Brandon on 2015-01-19.
 */
public class IndexInterpreter extends Interpreter<IndexValue> implements Opcodes {
    private final InsnList instructions;

    public IndexInterpreter(MethodNode method) {
        super(Opcodes.ASM5);
        this.instructions = method.instructions;
    }


    @Override
    public IndexValue newValue(Type type) {
        return newValue(type, -1);
    }

    public IndexValue newValue(Type type, int index) {
        if (type == null) {
            return new IndexValue(null, index);
        }
        switch (type.getSort()) {
            case Type.VOID:
                return null;
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                return new IndexValue(Type.INT_TYPE, index);
            case Type.FLOAT:
                return new IndexValue(Type.FLOAT_TYPE, index);
            case Type.LONG:
                return new IndexValue(Type.LONG_TYPE, index);
            case Type.DOUBLE:
                return new IndexValue(Type.DOUBLE_TYPE, index);
            case Type.ARRAY:
            case Type.OBJECT:
                return new IndexValue(Type.getObjectType("java/lang/Object"), index);
            default:
                throw new Error("Internal error");
        }
    }

    @Override
    public IndexValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
        int index = instructions.indexOf(insn);
        switch (insn.getOpcode()) {
            case ACONST_NULL:
                return newValue(Type.getObjectType("null"), index);
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
                return new IndexValue(Type.INT_TYPE, index);
            case LCONST_0:
            case LCONST_1:
                return new IndexValue(Type.LONG_TYPE, index);
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
                return new IndexValue(Type.FLOAT_TYPE, index);
            case DCONST_0:
            case DCONST_1:
                return new IndexValue(Type.DOUBLE_TYPE, index);
            case BIPUSH:
            case SIPUSH:
                return new IndexValue(Type.INT_TYPE, index);
            case LDC:
                Object cst = ((LdcInsnNode) insn).cst;
                if (cst instanceof Integer) {
                    return new IndexValue(Type.INT_TYPE, index);
                } else if (cst instanceof Float) {
                    return new IndexValue(Type.FLOAT_TYPE, index);
                } else if (cst instanceof Long) {
                    return new IndexValue(Type.LONG_TYPE, index);
                } else if (cst instanceof Double) {
                    return new IndexValue(Type.DOUBLE_TYPE, index);
                } else if (cst instanceof String) {
                    return newValue(Type.getObjectType("java/lang/String"), index);
                } else if (cst instanceof Type) {
                    int srt = ((Type) cst).getSort();
                    if (srt != LCONST_0 && srt != LCONST_1) {
                        if (srt == FCONST_0) {
                            return newValue(Type.getObjectType("java/lang/invoke/MethodType"), index);
                        }
                        throw new IllegalArgumentException("Illegal LDC constant " + cst);
                    }

                    return newValue(Type.getObjectType("java/lang/Class"), index);
                } else {
                    if (cst instanceof Handle) {
                        return newValue(Type.getObjectType("java/lang/invoke/MethodHandle"), index);
                    }

                    throw new IllegalArgumentException("Illegal LDC constant " + cst);
                }
            case JSR:
                return new IndexValue(Type.VOID_TYPE, index);
            case GETSTATIC:
                return newValue(Type.getType(((FieldInsnNode) insn).desc), index);
            case NEW:
                return newValue(Type.getObjectType(((TypeInsnNode) insn).desc), index);
            default:
                throw new Error("Internal error.");
        }
    }

    @Override
    public IndexValue copyOperation(AbstractInsnNode insn, IndexValue indexValue) throws AnalyzerException {
        return indexValue;
    }

    @Override
    public IndexValue unaryOperation(AbstractInsnNode insn, IndexValue indexValue) throws AnalyzerException {
        int index = instructions.indexOf(insn);
        switch (insn.getOpcode()) {
            case INEG:
            case IINC:
            case L2I:
            case F2I:
            case D2I:
            case I2B:
            case I2C:
            case I2S:
                return new IndexValue(Type.INT_TYPE, index);
            case FNEG:
            case I2F:
            case L2F:
            case D2F:
                return new IndexValue(Type.LONG_TYPE, index);
            case LNEG:
            case I2L:
            case F2L:
            case D2L:
                return new IndexValue(Type.FLOAT_TYPE, index);
            case DNEG:
            case I2D:
            case L2D:
            case F2D:
                return new IndexValue(Type.DOUBLE_TYPE, index);
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case TABLESWITCH:
            case LOOKUPSWITCH:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case PUTSTATIC:
                return null;
            case GETFIELD:
                return newValue(Type.getType(((FieldInsnNode) insn).desc), index);
            case NEWARRAY:
                switch (((IntInsnNode) insn).operand) {
                    case 4:
                        return this.newValue(Type.getType("[Z"), index);
                    case 5:
                        return this.newValue(Type.getType("[C"), index);
                    case 6:
                        return this.newValue(Type.getType("[F"), index);
                    case 7:
                        return this.newValue(Type.getType("[D"), index);
                    case 8:
                        return this.newValue(Type.getType("[B"), index);
                    case 9:
                        return this.newValue(Type.getType("[S"), index);
                    case 10:
                        return this.newValue(Type.getType("[I"), index);
                    case 11:
                        return this.newValue(Type.getType("[J"), index);
                    default:
                        throw new AnalyzerException(insn, "Invalid array type");
                }
            case ANEWARRAY:
                String desc = ((TypeInsnNode) insn).desc;
                return newValue(Type.getType("[" + Type.getObjectType(desc)), index);
            case ARRAYLENGTH:
                return new IndexValue(Type.INT_TYPE, index);
            case ATHROW:
                return null;
            case CHECKCAST:
                desc = ((TypeInsnNode) insn).desc;
                return newValue(Type.getObjectType(desc), index);
            case INSTANCEOF:
                return new IndexValue(Type.INT_TYPE, index);
            case MONITORENTER:
            case MONITOREXIT:
            case IFNULL:
            case IFNONNULL:
                return null;
            default:
                throw new Error("Internal error.");
        }
    }

    @Override
    public IndexValue binaryOperation(AbstractInsnNode insn, IndexValue indexValue, IndexValue v1) throws AnalyzerException {
        int index = instructions.indexOf(insn);
        switch (insn.getOpcode()) {
            case IALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
            case IADD:
            case ISUB:
            case IMUL:
            case IDIV:
            case IREM:
            case ISHL:
            case ISHR:
            case IUSHR:
            case IAND:
            case IOR:
            case IXOR:
                return new IndexValue(Type.INT_TYPE, index);
            case LALOAD:
            case LADD:
            case LSUB:
            case LMUL:
            case LDIV:
            case LREM:
            case LSHL:
            case LSHR:
            case LUSHR:
            case LAND:
            case LOR:
            case LXOR:
                return new IndexValue(Type.LONG_TYPE, index);
            case FALOAD:
            case FADD:
            case FSUB:
            case FMUL:
            case FDIV:
            case FREM:
                return new IndexValue(Type.FLOAT_TYPE, index);
            case DALOAD:
            case DADD:
            case DSUB:
            case DMUL:
            case DDIV:
            case DREM:
                return new IndexValue(Type.DOUBLE_TYPE, index);
            case AALOAD:
                return new IndexValue(Type.getObjectType("java/lang/Object"), index);
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
                return new IndexValue(Type.INT_TYPE, index);
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case PUTFIELD:
                return null;
            default:
                throw new Error("Internal Error.");
        }
    }

    @Override
    public IndexValue ternaryOperation(AbstractInsnNode insn, IndexValue indexValue, IndexValue v1, IndexValue v2) throws AnalyzerException {
        return null;
    }

    @Override
    public IndexValue naryOperation(AbstractInsnNode insn, List<? extends IndexValue> list) throws AnalyzerException {
        if (insn.getOpcode() == MULTIANEWARRAY) {
            return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc), instructions.indexOf(insn));
        } else {
            return newValue(Type.getReturnType(((MethodInsnNode) insn).desc), instructions.indexOf(insn));
        }
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, IndexValue indexValue, IndexValue v1) throws AnalyzerException {
    }

    @Override
    public IndexValue merge(IndexValue indexValue, IndexValue v1) {
        return !indexValue.equals(v1) ? new IndexValue(null) : indexValue;
    }
}
