package org.acid.updater.modifiers.visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by Brandon on 2015-01-15.
 */
public abstract class AbstractClassVisitor extends ClassVisitor implements Opcodes {
    private final IClassVisitor visitor;
    private final int access;
    private final String name;
    private final String desc;
    private final String signature;
    private final String[] exceptions;
    private final boolean add;
    private boolean fieldExists;
    private boolean methodExists;

    public AbstractClassVisitor(ClassVisitor visitor, int access, String name, String desc, boolean add) {
        this(new IStaticClassVisitor(visitor), access, name, desc, add);
    }

    public AbstractClassVisitor(IClassVisitor visitor, int access, String name, String desc, boolean add) {
        this(visitor, access, name, desc, null, null, add);
    }

    public AbstractClassVisitor(IClassVisitor visitor, int access, String name, String desc, String signature, String[] exceptions, boolean add) {
        super(ASM5, visitor.getVisitor());
        this.visitor = visitor;
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.exceptions = exceptions;
        this.add = add;
        this.fieldExists = false;
        this.methodExists = false;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (name.equals(this.name) && desc.equals(this.desc)) {
            this.fieldExists = true;
            if (!add) {
                return null;
            }
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(this.name) && desc.equals(this.desc)) {
            this.methodExists = true;
            if (!add) {
                return null;
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        this.modify();
        super.visitEnd();
        visitor.apply(cv);
    }

    protected abstract void modify();

    protected void modifyField() {
        if (!this.fieldExists) {
            if (this.add) {
                FieldVisitor fv = super.visitField(this.access, this.name, this.desc, null, null);
                fv.visitEnd();
            }
        }
    }

    protected void modifyMethod() {
        if (!this.methodExists) {
            if (this.add) {
                MethodVisitor mv = super.visitMethod(this.access, this.name, this.desc, this.signature, this.exceptions);
                if (mv != null) {
                    mv.visitCode();
                    methodBody(mv);
                    mv.visitEnd();
                }
            }
        }
    }

    protected void methodBody(MethodVisitor mv) {

    }
}
