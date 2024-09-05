package acid.other;

import acid.visitors.ClassCheckWriter;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by Kira on 2014-12-06.
 */
public class JarParser {
    private ArrayList<ClassNode> classes = null;
    private String path = null;
    private Manifest manifest = null;
    private boolean is_android = false;

    public JarParser(String path) {
        this.path = path;
    }

    public int getHash() {
        return manifest != null ? manifest.hashCode() : -1;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    public void setClasses(ArrayList<ClassNode> classes) {
        this.classes = classes;
    }

    public ArrayList<ClassNode> getClasses() {
        return classes;
    }

    public boolean isAndroid() {
        if (classes == null) {
            return false;
        }
        return is_android;
    }

    public ArrayList<ClassNode> load() {
        try {
            JarFile file = new JarFile(path);
            classes = readJar(file);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    public void save() {
        writeJar(new File(path));
    }

    private ArrayList<ClassNode> readJar(JarFile file) {
        ArrayList<ClassNode> result = new ArrayList<>();

        file.stream().forEach(entry -> {
            List<String> bad_files = Arrays.asList(new String[]{
                    "__MACOSX",
                    "android",
                    "androidx",
                    "google",
                    "butterknife",
                    "appsflyer",
                    "com/android",
                    "com/google",
                    "com/butterknife",
                    "com/appsflyer",
                    "com/jagex/android",
                    "com/jagex3",
                    "com/jagex/mobilesdk",
                    "com/jagex/oldscape/android",
                    "com/jagex/oldscape/osrenderer",
                    "org"
            });

             if (bad_files.stream().anyMatch(e -> entry.getName().startsWith(e))) {
                 this.is_android = true;
                 return;
             }

            if (entry.getName().endsWith(".class")) {
                try {
                    this.manifest = file.getManifest();
                    ClassReader reader = new ClassReader(file.getInputStream(entry));
                    ClassNode node = new ClassNode();
                    reader.accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    result.add(node);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return result;
    }

    private void writeJar(File file) {
        try (JarOutputStream jos = this.manifest != null ? new JarOutputStream(new FileOutputStream(file), this.manifest) : new JarOutputStream(new FileOutputStream(file))) {
            for (ClassNode entry : classes) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                entry.accept(new ClassCheckWriter(Opcodes.ASM5, writer, false));
                byte[] bytes = writer.toByteArray();
                jos.putNextEntry(new JarEntry(entry.name.concat(".class")));
                jos.write(bytes);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
