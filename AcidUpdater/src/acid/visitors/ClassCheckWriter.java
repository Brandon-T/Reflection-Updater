package acid.visitors;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.util.CheckClassAdapter;

/**
 * Created by Kira on 2015-01-12.
 */
public class ClassCheckWriter extends ClassVisitor {
    private boolean debug;

    public ClassCheckWriter(int api, ClassWriter cv, boolean debug) {
        super(api, new CheckClassAdapter(cv, true));
        this.debug = debug;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (this.debug) {
            System.out.println("\nClass: " + name);
            System.out.println("-----------------------------------");
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (this.debug) {
            System.out.println("Method: " + name + " " + desc);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
