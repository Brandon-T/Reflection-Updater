package org.acid.updater;

import org.acid.updater.classloaders.ClassNodeLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class Acid {
    public interface Map<K,V> {
        default V replace(K key, V value) {
            V curValue;
            if (((curValue = get(key)) != null) || containsKey(key)) {
                curValue = put(key, value);
            }
            return curValue;
        }

        boolean	containsKey(Object key);
        V get(Object key);
        V put(K key, V value);
    }


    public void print() {

        try {
            MethodNode init = new MethodNode();
            init.name = "<init>";
            init.desc = "()V";
            init.access = Opcodes.ACC_PUBLIC;
            init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
            init.instructions.add(new InsnNode(Opcodes.RETURN));
            init.exceptions = new ArrayList<>();


            MethodNode mm = new MethodNode();
            mm.name = "test";
            mm.desc = "()V";
            mm.access = Opcodes.ACC_NATIVE | Opcodes.ACC_PUBLIC;
            mm.exceptions = new ArrayList<>();


            ClassNode node = new ClassNode();
            node.version = 52;
            node.sourceFile = "MyClass.java";
            node.name = "MyClass";
            node.superName = "java/lang/Object";
            node.methods.add(mm);
            node.methods.add(init);

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(cw);

            ClassNodeLoader loader = new ClassNodeLoader();
            Class ccl = loader.nodeToClass(node);

            ccl.newInstance();

            node = new ClassNode();
            ClassReader cr = new ClassReader(cw.toByteArray());
            cr.accept(node, 0);

            node.methods.stream().forEach(m -> {
                System.out.println("\n\nMethod: " + m.name + "" + m.desc + "\n");
                System.out.println("-------------------------------\n");

                Printer printer = new Textifier();
                TraceMethodVisitor visitor = new TraceMethodVisitor(printer);
                Arrays.stream(m.instructions.toArray()).forEachOrdered(instruction -> {
                    instruction.accept(visitor);
                    StringWriter writer = new StringWriter();
                    printer.print(new PrintWriter(writer));
                    printer.getText().clear();
                    System.out.print(writer.toString());
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            ClassNode node = loadRelativeClassNode(Map.class.getName());
            node.methods.stream().filter(m -> m.name.equals("replace")).forEach(m -> {

                System.out.println("\n\nMethod: " + m.name + "" + m.desc + "\n");
                System.out.println("-------------------------------\n");

                Printer printer = new Textifier();
                TraceMethodVisitor visitor = new TraceMethodVisitor(printer);
                Arrays.stream(m.instructions.toArray()).forEachOrdered(instruction -> {
                    instruction.accept(visitor);
                    StringWriter writer = new StringWriter();
                    printer.print(new PrintWriter(writer));
                    printer.getText().clear();
                    System.out.print(writer.toString());
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ClassNode loadJVMClassNode(String cls) throws IOException, ClassNotFoundException {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Class clz = loader.loadClass(cls);
        InputStream url = clz.getResourceAsStream(clz.getSimpleName() + ".class");
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(url);
        reader.accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return node;
    }

    private static ClassNode loadRelativeClassNode(String cls) throws IOException, ClassNotFoundException {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Class clz = loader.loadClass(cls);
        InputStream url = clz.getResourceAsStream(("./" + clz.getName() + ".class").replace(clz.getPackage().getName() + ".", ""));
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(url);
        reader.accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return node;
    }
}
