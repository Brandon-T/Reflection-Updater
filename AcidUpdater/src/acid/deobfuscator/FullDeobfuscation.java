package acid.deobfuscator;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Kira on 2014-12-10.
 */
public class FullDeobfuscation {
    private Collection<Deobfuscator> deobfuscators;
    private acid.deobfuscator.DeadCodeRemover deadCodeRemover;

    public FullDeobfuscation(Collection<ClassNode> classes, boolean is_android) {
        this.deobfuscators = new ArrayList<>();
        this.deobfuscators.add(is_android ? new AndroidMethodRemover(classes) : new MethodRemover(classes));
        this.deobfuscators.add(new FieldRemover(classes));
        this.deobfuscators.add(new ClassRemover(classes));
        this.deobfuscators.add(new Normaliser(classes));
        this.deobfuscators.add(new ExceptionRemover(classes));
        this.deobfuscators.add(new ReturnRemover(classes));
        this.deobfuscators.add(new TryCatchRemover(classes));
        this.deobfuscators.add(new TryCatchRemover2(classes));
//        this.deobfuscators.add(new ParameterRemover(classes));
        this.deobfuscators.add(new AggressiveParameterRemover(classes));
        this.deadCodeRemover = new DeadCodeRemover(classes);
    }

    public FullDeobfuscation analyse() {
        System.out.println("Deobfuscation Started..");
        deobfuscators.stream().forEach(d -> d.analyse());
        return this;
    }

    public void remove() {
        deobfuscators.forEach(d -> d.remove());
        this.deadCodeRemover.analyse().remove();
        System.out.println("Deobfuscation Finished..\n");
    }
}
