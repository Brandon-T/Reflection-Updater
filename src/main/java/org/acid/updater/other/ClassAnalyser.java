package org.acid.updater.other;

import org.acid.updater.analysers.LinkedList;
import org.acid.updater.analysers.Queue;
import org.acid.updater.analysers.*;
import org.acid.updater.deobfuscator.FullDeobfuscation;
import org.acid.updater.structures.ClassInfo;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.util.*;
import java.util.jar.Manifest;

/**
 * Created by Brandon on 2014-12-14.
 */
public class ClassAnalyser {

    private static final String OriginalPackPath = "./Gamepacks/Originals/";
    private static final String DeobfuscatedPackPath = "./Gamepacks/Deobs/";

    private Manifest manifest = null;
    private ArrayList<ClassNode> classes = null;
    private LinkedHashMap<String, ClassInfo> found = null;
    private ArrayList<Analyser> analysers = null;
    private boolean is_android = false;
    private boolean didAnalyse = false;


    public ClassAnalyser(String url, String jar, boolean deobfuscate) {
        if (!deobfuscate) {
            loadOriginalPack(url, jar);
        } else {
            loadDeobfuscatedPack(url, jar);
        }

        loadAnalysers();
    }

    public ClassAnalyser analyse() {
        if (didAnalyse) {
            return this;
        }

        didAnalyse = true;

        found.clear();
        analysers.stream().forEach(a -> {
            //System.out.println("Analysing: " + a.getClass().getSimpleName());

            ClassNode n = a.find(classes);
            if (n != null) {
                ClassInfo info = a.analyse(n);
                found.put(info.getId().toLowerCase(), info);
            } else if (!a.getClass().getSimpleName().equals(Other.class.getSimpleName())) {
                System.out.println("Failed to find: " + a.getClass().getSimpleName());
            }
        });
        return this;
    }

    public void print() {
        this.analyse();
        found.forEach((key, value) -> System.out.println(value));
    }

    public void printNative() {
        found.forEach((key, value) -> System.out.println(value.toNativeHeaderString()));
        found.forEach((key, value) -> System.out.println(value.toNativeString()));
    }

    public void printSimba() {
        final String[] result = {""};
        found.forEach((key, value) -> {
            result[0] += value.toSimbaString() + "\n";
        });

        System.out.println("const\n" +
                "    ReflectionRevision = '" + found.get("client").getField("Revision").getName() + "';\n");
        System.out.println(result[0]);
    }

    public void printSimbaNative() {
        this.analyse();

        final String[] result = {""};
        found.forEach((key, value) -> {
            result[0] += value.toSimbaNativeString() + "\n";
        });

        System.out.println("const\n" +
                "    ReflectionRevision = '" + found.get("client").getField("Revision").getName() + "';\n");
        System.out.println(result[0]);
    }

    public String getSimba() {
        final String[] result = {""};
        found.forEach((key, value) -> result[0] += value.toSimbaString() + "\n");

        return "const\n" +
                "    ReflectionRevision = '" + found.get("client").getField("Revision").getName() + "';\n\n" +
                result[0];
    }

    public void printPython() {
        this.analyse();

        final String[] result = {""};
        List<ClassInfo> found = this.found.values().stream().toList();
        found.forEach(value -> {
            if (value == found.getLast()) {
                result[0] += value.toPythonString(found);
            } else {
                result[0] += value.toPythonString(found);
            }
        });

        System.out.println(result[0]);
    }

    public void printJSON() {
        this.analyse();

        final String[] result = {"["};
        List<ClassInfo> found = this.found.values().stream().toList();
        found.forEach(value -> {
            if (value == found.getLast()) {
                result[0] += value.toJSONString();
            } else {
                result[0] += value.toJSONString() + ",\n";
            }
        });
        result[0] += "]";
        System.out.println(result[0]);
    }

    public void refactor(String jar) {
        this.analyse();

        getFound().forEach((key, value) -> {
            value.refactor(getClasses());
        });

        JarParser parser = new JarParser(DeobfuscatedPackPath + jar);
        parser.setManifest(manifest);
        parser.setClasses(classes);
        parser.save();
    }

    private ClassAnalyser loadAnalysers() {
        analysers.add(new Node());
        analysers.add(new NodeDeque());
        analysers.add(new CacheableNode());
        analysers.add(new IndexedNode());
        analysers.add(new LinkedList());
        analysers.add(new HashTable());
        analysers.add(new IterableHashTable());
        analysers.add(new Queue());
        analysers.add(new Cache());
        analysers.add(new ClassData());
        analysers.add(new Rasteriser());
        analysers.add(new Rasteriser3D());
        analysers.add(new Typeface());
        analysers.add(new IndexedImage());
        analysers.add(new ImageRGB());
//        analysers.add(new GraphicsBuffer());
        analysers.add(new Font());
        analysers.add(new SpriteMask());
        analysers.add(new PacketWriter());
        analysers.add(new Keyboard());
        analysers.add(new GameShell());
        analysers.add(new Buffer());
        analysers.add(new PacketBuffer());
//        analysers.add(new BufferedConnection());
        analysers.add(new CollisionMap());
        analysers.add(new NameInfo());
        analysers.add(new Renderable());
        analysers.add(new DynamicObject());
        analysers.add(new Scene());
        analysers.add(new BoundaryObject());
        analysers.add(new WallDecoration());
        analysers.add(new FloorDecoration());
        analysers.add(new GameObject());
        analysers.add(new GraphicsObject());
        analysers.add(new SceneTile());
        analysers.add(new GrandExchangeOffer());
        analysers.add(new Model());
        analysers.add(new AnimationFrames());
        analysers.add(new AnimationSequence());
        analysers.add(new AnimationSkeleton());
        analysers.add(new Animation());
        analysers.add(new CombatInfo1());
        analysers.add(new CombatInfo2());
        analysers.add(new CombatInfoList());
        analysers.add(new CombatInfoHolder());
        analysers.add(new Actor());
        analysers.add(new NPCDefinition());
        analysers.add(new NPC());
        analysers.add(new PlayerDefinition());
        analysers.add(new Player());
        analysers.add(new ObjectDefinition());
        analysers.add(new Projectile());
        analysers.add(new WidgetNode());
        analysers.add(new Widget());
        analysers.add(new WidgetHolder());
        analysers.add(new ItemDefinition());
        analysers.add(new Item());
        analysers.add(new ItemNode());
        analysers.add(new Login());
        analysers.add(new Varps());
        analysers.add(new Varcs());
        analysers.add(new VarbitDefinition());
        analysers.add(new GameInstance());
        analysers.add(new PlayerUpdateManager());
        analysers.add(new Menu());
        analysers.add(new Client());
        analysers.add(new Other());
        return this;
    }

    private void loadOriginalPack(String url, String jar) {
        if (!new java.io.File(OriginalPackPath + jar).exists()) {
            new java.io.File(OriginalPackPath).mkdirs();
            System.out.println("Downloading JarFile.");
            new JarDownloader(url, OriginalPackPath + jar);
            System.out.println("Downloading Complete.\n");
        }
        loadPack(OriginalPackPath + jar);
    }

    private void loadDeobfuscatedPack(String url, String jar) {
        if (!new File(DeobfuscatedPackPath + jar).exists()) {
            loadOriginalPack(url, jar);
            new java.io.File(DeobfuscatedPackPath).mkdirs();
            new FullDeobfuscation(classes, is_android).analyse().remove();
            savePack(DeobfuscatedPackPath + jar);
        } else {
            loadPack(DeobfuscatedPackPath + jar);
        }
    }

    private void loadPack(String jar) {
        JarParser parser = new JarParser(jar);
        classes = parser.load();
        manifest = parser.getManifest();
        analysers = new ArrayList<>();
        found = new LinkedHashMap<>();
        is_android = parser.isAndroid();
    }

    private void savePack(String jar) {
        JarParser parser = new JarParser(jar);
        parser.setManifest(manifest);
        parser.setClasses(classes);
        parser.save();
    }

    public final LinkedHashMap<String, ClassInfo> getFound() {
        return found;
    }

    public final String getJarHash() {
        return String.valueOf(manifest != null ? manifest.hashCode() : -1);
    }

    public final String getClassName(String name) {
        ClassInfo result = found.getOrDefault(name.toLowerCase(), null);
        return result != null ? result.getName() : null;
    }

    public ClassInfo getInfo(String name) {
        return found.getOrDefault(name.toLowerCase(), null);
    }

    public final ClassNode getClass(String name) {
        for (ClassNode n : classes) {
            if (n.name.equals(name)) {
                return n;
            }
        }
        return null;
    }

    public final ClassNode getClassNode(String name) {
        String cls_name = getClassName(name);
        for (ClassNode n : classes) {
            if (n.name.equals(cls_name)) {
                return n;
            }
        }
        return null;
    }

    public final ArrayList<ClassNode> getClasses() {
        return classes;
    }

    public final void findField(String className, String field) {
        classes.stream().forEach(n -> {
            n.methods.stream().forEach(m -> {
                for (AbstractInsnNode a : m.instructions.toArray()) {
                    if (a instanceof FieldInsnNode f) {

                        if (field != null) {
                            if ((f.owner.equals(className) || findSuperField(n, className)) && f.name.equals(field)) {
                                System.out.println("Class: " + n.name + "  Method -> " + m.name + "  " + m.desc);
                            }
                        } else {
                            if ((f.owner.equals(className) || findSuperField(n, className))) {
                                System.out.println("Class: " + n.name + "  Method -> " + m.name + "  " + m.desc);
                            }
                        }
                    }
                }
            });
        });
    }

    public final void findFieldDescription(String fieldDesc) {
        classes.stream().forEach(n -> {
            n.methods.stream().forEach(m -> {
                for (AbstractInsnNode a : m.instructions.toArray()) {
                    if (a instanceof FieldInsnNode f) {

                        if (f.desc.equals(fieldDesc)) {
                            System.out.println("Class: " + n.name + "  Method -> " + m.name + "  " + m.desc + " Field -> " + f.name);
                        }
                    }
                }
            });
        });
    }

    public final void findMethod(String className, String superName, String name, String desc) {
        classes.stream().forEach(n -> {
            n.methods.stream().forEach(m -> {
                boolean matches = className == null || n.name.equals(className);

                if (superName != null) {
                    matches = n.superName.equals(superName) && matches;
                }

                if (name != null) {
                    matches = m.name.equals(name) && matches;
                }

                if (desc != null) {
                    matches = m.desc.equals(desc) && matches;
                }

                if (matches) {
                    System.out.println("Class: " + n.name + "  Method -> " + m.name + "  " + m.desc);
                }
            });
        });
    }

    public void findMethodUsage(String className, String name, String desc) {
        classes.stream().forEach(n -> {
            n.methods.stream().forEach(m -> {
                for (AbstractInsnNode a : m.instructions.toArray()) {
                    if (a instanceof MethodInsnNode f) {

                        if (desc != null) {
                            if ((f.owner.equals(className) || findSuperField(n, className)) && f.name.equals(name) && f.desc.equals(desc)) {
                                System.out.println("Class: " + n.name + "  Method -> " + m.name + "  " + m.desc);
                            }
                        } else {
                            if ((f.owner.equals(className) || findSuperField(n, className)) && f.name.equals(name)) {
                                System.out.println("Class: " + n.name + "  Method -> " + m.name + "  " + m.desc);
                            }
                        }
                    }
                }
            });
        });
    }

    public final long findMultiplier(String owner, String field) {
        ArrayList<Long> multipliers = new ArrayList<>();

        for (ClassNode n : classes) {
            for (MethodNode m : n.methods) {
                long multi = new Finder(m).findMultiplier(owner, field);
                if (multi != 0 && multi != 1 && multi % 2 != 0) {
                    multipliers.add(multi);
                }
            }
        }

        long highest = 0;
        int cur, max = 0;
        HashSet<Long> unique = new HashSet<Long>(multipliers);

        for (long key : unique) {
            cur = Collections.frequency(multipliers, key);
            if (max < cur) {
                max = cur;
                highest = key;
            }
        }
        return highest == 0 ? 1 : highest;
    }

    private final boolean findSuperField(ClassNode node, String owner) {
        ClassNode n = node;
        while (n != null && !n.superName.equals("java/lang/Object") && !n.superName.contains("java")) {
            if (n.superName.equals(owner)) {
                return true;
            }

            n = findClass(n.superName);
        }
        return false;
    }

    private final ClassNode findClass(String name) {
        for (ClassNode n : classes) {
            if (n.name.equals(name)) {
                return n;
            }
        }
        return null;
    }
}
