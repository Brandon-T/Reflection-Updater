package acid.modifiers.commons;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.function.Function;

/**
 * Created by Kira on 2015-01-15.
 */
public class DynamicModifier {
    public DynamicModifier(ClassNode node, Function<ClassNode, ClassVisitor> factory) {
        inlineCopy(node, dynamicModify(node, factory));
    }

    private static ClassNode dynamicModify(ClassNode node, Function<ClassNode, ClassVisitor> factory) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(cw);
        ClassReader cr = new ClassReader(cw.toByteArray());
        cr.accept(factory.apply(node = new ClassNode()), 0);
        return node;
    }

    private static void inlineCopy(ClassNode nodeToModify, ClassNode nodeToCopy) {
        nodeToModify.version = nodeToCopy.version;
        nodeToModify.access = nodeToCopy.access;
        nodeToModify.name = nodeToCopy.name;
        nodeToModify.signature = nodeToCopy.signature;
        nodeToModify.superName = nodeToCopy.superName;
        nodeToModify.interfaces = nodeToCopy.interfaces;
        nodeToModify.sourceFile = nodeToCopy.sourceFile;
        nodeToModify.sourceDebug = nodeToCopy.sourceDebug;
        nodeToModify.outerClass = nodeToCopy.outerClass;
        nodeToModify.outerMethod = nodeToCopy.outerMethod;
        nodeToModify.outerMethodDesc = nodeToCopy.outerMethodDesc;
        nodeToModify.visibleAnnotations = nodeToCopy.visibleAnnotations;
        nodeToModify.invisibleAnnotations = nodeToCopy.invisibleAnnotations;
        nodeToModify.visibleTypeAnnotations = nodeToCopy.visibleTypeAnnotations;
        nodeToModify.invisibleTypeAnnotations = nodeToCopy.invisibleTypeAnnotations;
        nodeToModify.attrs = nodeToCopy.attrs;
        nodeToModify.innerClasses = nodeToCopy.innerClasses;
        nodeToModify.fields = nodeToCopy.fields;
        nodeToModify.methods = nodeToCopy.methods;
    }
}
