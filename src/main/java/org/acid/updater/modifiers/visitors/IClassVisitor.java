package org.acid.updater.modifiers.visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

/**
 * Created by Brandon on 2015-01-15.
 */
public abstract class IClassVisitor {
    private final ClassVisitor visitor;

    public IClassVisitor(ClassVisitor visitor) {
        this.visitor = visitor;
    }

    protected ClassVisitor getVisitor() {
        return visitor;
    }

    protected abstract void apply(ClassVisitor visitor);

    protected void inlineCopy(ClassNode nodeToModify, ClassNode nodeToCopy) {
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
