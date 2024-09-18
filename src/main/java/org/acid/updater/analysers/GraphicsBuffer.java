package org.acid.updater.analysers;

import org.acid.updater.structures.ClassField;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
 * Created by Brandon on 2014-12-15.
 */
public class GraphicsBuffer extends Analyser {
    @Override
    public ClassNode find(Collection<ClassNode> nodes) {
        for (ClassNode n : nodes) {
            if (!n.interfaces.contains("java/lang/image/ImageProducer") && !n.interfaces.contains("java/awt/image/ImageObserver")) {
                continue;
            }

            return n;
        }
        return null;
    }

    @Override
    public ClassInfo analyse(ClassNode node) {
        ClassInfo info = new ClassInfo("GraphicsBuffer", node);
        info.putField(findColourModel(node));
        info.putField(findImageConsumer(node));
        info.putField(findCreateGraphicsBuffer(node));
        return info;
    }

    private ClassField findColourModel(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("Ljava/awt/image/ColorModel;")) {
                return new ClassField("ColourModel", f.name, f.desc);
            }
        }
        return new ClassField("ColourModel");
    }

    private ClassField findImageConsumer(ClassNode node) {
        for (FieldNode f : node.fields) {
            if (f.desc.equals("Ljava/awt/image/ImageConsumer;")) {
                return new ClassField("ImageConsumer", f.name, f.desc);
            }
        }
        return new ClassField("ImageConsumer");
    }

    private ClassField findCreateGraphicsBuffer(ClassNode node) {
        for (MethodNode m : node.methods) {
            if (m.desc.equals("(IILjava/awt/Component;)V")) {
                return new ClassField("*CreateBuffer", m.name, m.desc);
            }
        }
        return new ClassField("*CreateBuffer");
    }
}
