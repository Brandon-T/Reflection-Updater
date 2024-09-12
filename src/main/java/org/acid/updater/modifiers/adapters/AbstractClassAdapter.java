package org.acid.updater.modifiers.adapters;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/**
 * Created by Brandon on 2015-01-15.
 */
public abstract class AbstractClassAdapter extends ClassNode implements Opcodes {
    private final IClassAdapter adapter;
    private final int access;
    private final String name;
    private final String desc;
    private final String signature;
    private final String[] exceptions;
    private final boolean add;
    private boolean fieldExists;
    private boolean methodExists;

    public AbstractClassAdapter(ClassVisitor visitor, int access, String name, String desc, boolean add) {
        this(new IStaticClassAdapter(visitor), access, name, desc, null, null, add);
    }

    public AbstractClassAdapter(IClassAdapter adapter, int access, String name, String desc, boolean add) {
        this(adapter, access, name, desc, null, null, add);
    }

    public AbstractClassAdapter(IClassAdapter adapter, int access, String name, String desc, String signature, String[] exceptions, boolean add) {
        super(ASM5);
        this.adapter = adapter;
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
        this.modifyMethod();
        this.adapter.apply(this);
        super.visitEnd();
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
