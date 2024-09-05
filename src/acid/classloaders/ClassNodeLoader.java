package acid.classloaders;

/**
 * Created by Kira on 2015-01-16.
 */
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.HashMap;

public class ClassNodeLoader extends ClassLoader {

    private HashMap<String, ClassNode> classes = new HashMap<>();

    public static ClassNode loadClassNode(Class cls) throws IOException {
        InputStream stream = cls.getResourceAsStream(cls.getSimpleName() + ".class");
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(stream);
        reader.accept(node, 0);
        return node;
    }

    public static ClassNode loadClassNode(String cls) throws IOException, ClassNotFoundException {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Class clz = loader.loadClass(cls);
        InputStream url = clz.getResourceAsStream(clz.getSimpleName() + ".class");
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(url);
        reader.accept(node, 0);
        return node;
    }

    public static ClassNode loadRelativeClassNode(String cls) throws IOException, ClassNotFoundException {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Class clz = loader.loadClass(cls);
        InputStream url = clz.getResourceAsStream(("./" + clz.getName() + ".class").replace(clz.getPackage().getName() + ".", ""));
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(url);
        reader.accept(node, 0);
        return node;
    }


    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith("java")) {
            return super.loadClass(name);
        }
        return findClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        ClassNode node = classes.get(name.replace('.', '/'));
        return node == null ? super.findClass(name) : nodeToClass(node);
    }

    public final void addNode(ClassNode node) {
        classes.put(node.name, node);
    }

    public final Class<?> nodeToClass(ClassNode node) {
        if (super.findLoadedClass(node.name) != null) {
            return findLoadedClass(node.name);
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(cw);
        byte[] b = cw.toByteArray();
        return defineClass(node.name.replace('/', '.'), b, 0, b.length, getDomain());
    }

    private final ProtectionDomain getDomain() {
        CodeSource code = new CodeSource(null, (Certificate[]) null);
        return new ProtectionDomain(code, getPermissions());
    }

    private final Permissions getPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new AllPermission());
        return permissions;
    }

}
