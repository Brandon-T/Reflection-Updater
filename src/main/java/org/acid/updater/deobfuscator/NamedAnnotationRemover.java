package org.acid.updater.deobfuscator;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;

public class NamedAnnotationRemover extends Deobfuscator {
    private int class_annotation_count = 0;
    private int field_annotation_count = 0;
    private int method_annotation_count = 0;

    int removed_class_annotation_count = 0;
    int removed_field_annotation_count = 0;
    int removed_method_annotation_count = 0;

    public NamedAnnotationRemover(Collection<ClassNode> classes) {
        super(classes);
    }

    public NamedAnnotationRemover analyse() {
        classes.stream().forEach(node -> {
            if (node.invisibleAnnotations != null && !node.invisibleAnnotations.isEmpty()) {
                ++class_annotation_count;
            }

            node.fields.forEach(field -> {
                if (field.invisibleAnnotations != null && !field.invisibleAnnotations.isEmpty()) {
                    ++field_annotation_count;
                }
            });

            node.methods.forEach(method -> {
                if (method.invisibleAnnotations != null && !method.invisibleAnnotations.isEmpty()) {
                    ++method_annotation_count;
                }
            });
        });
        return this;
    }

    public void remove() {
        classes.stream().forEach(node -> {
            if (node.invisibleAnnotations != null && !node.invisibleAnnotations.isEmpty()) {
                for (AnnotationNode an : node.invisibleAnnotations.reversed()) {
                    if (an.desc.equals("Ljavax/inject/Named;")) {
                        node.invisibleAnnotations.remove(an);
                        ++removed_class_annotation_count;
                    }
                }

                if (node.invisibleAnnotations.isEmpty()) {
                    node.invisibleAnnotations = null;
                }
            }

            node.fields.forEach(field -> {
                if (field.invisibleAnnotations != null && !field.invisibleAnnotations.isEmpty()) {
                    for (AnnotationNode an : field.invisibleAnnotations.reversed()) {
                        if (an.desc.equals("Ljavax/inject/Named;")) {
                            field.invisibleAnnotations.remove(an);
                            ++removed_field_annotation_count;
                        }
                    }

                    if (field.invisibleAnnotations.isEmpty()) {
                        field.invisibleAnnotations = null;
                    }
                }
            });

            node.methods.forEach(method -> {
                if (method.invisibleAnnotations != null && !method.invisibleAnnotations.isEmpty()) {
                    for (AnnotationNode an : method.invisibleAnnotations.reversed()) {
                        if (an.desc.equals("Ljavax/inject/Named;")) {
                            method.invisibleAnnotations.remove(an);
                            ++removed_method_annotation_count;
                        }
                    }

                    if (method.invisibleAnnotations.isEmpty()) {
                        method.invisibleAnnotations = null;
                    }
                }
            });
        });

        System.out.println("Removed Class Annotations: " + removed_class_annotation_count + " of " + class_annotation_count);
        System.out.println("Removed Field Annotations: " + removed_field_annotation_count + " of " + field_annotation_count);
        System.out.println("Removed Method Annotations: " + removed_method_annotation_count + " of " + method_annotation_count);
    }
}
