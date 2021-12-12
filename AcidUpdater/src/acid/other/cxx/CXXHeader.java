package acid.other.cxx;

import acid.classloaders.ClassNodeLoader;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;

public class CXXHeader {
    private String className;
    private String superName;
    private ClassNode classNode;
    private ArrayList<CXXMethod> publicMethods;
    private ArrayList<CXXMethod> protectedMethods;


    public CXXHeader(String className) {
        try {
            this.classNode = ClassNodeLoader.loadClassNode(className);
            this.className = CXXUtilities.normalizeTypeDesc(this.classNode.name);
            this.superName = CXXUtilities.normalizeTypeDesc(this.classNode.superName);
            this.publicMethods = new ArrayList<>();
            this.protectedMethods = new ArrayList<>();

            //Construct public methods..
            classNode.methods.stream().filter((m) -> (m.access & Opcodes.ACC_PUBLIC) != 0).forEach(m -> {
                publicMethods.add(new CXXMethod(m));
            });

            //Construct protected methods..
            classNode.methods.stream().filter((m) -> (m.access & Opcodes.ACC_PROTECTED) != 0).forEach(m -> {
                protectedMethods.add(new CXXMethod(m));
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getClassName() {
        return className;
    }

    public ArrayList<CXXMethod> getPublicMethods() {
        return publicMethods;
    }

    public ArrayList<CXXMethod> getProtectedMethods() {
        return protectedMethods;
    }

    @Override
    public String toString() {
        //Construct the class structure..
        final StringBuilder builder = new StringBuilder();
        builder.append("class " + this.className + " : public " + this.superName).append("\n");
        builder.append("{").append("\n");

        if (this.publicMethods.size() > 0) {
            builder.append("    public:").append("\n");

            this.publicMethods.forEach((m) -> {
                builder.append("        ").append(m.getHeaderSignature()).append("\n");
            });
        }

        if (this.publicMethods.size() > 0 && this.protectedMethods.size() > 0) {
            builder.append("\n\n");
        }

        if (this.protectedMethods.size() > 0) {
            builder.append("    protected:").append("\n");
            this.protectedMethods.forEach((m) -> {
                builder.append("        ").append(m.getHeaderSignature()).append("\n");
            });
        }

        builder.append("}\n\n");
        return builder.toString();
    }
}
