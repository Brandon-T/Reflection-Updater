package org.acid.updater.structures;

import org.acid.updater.other.Renamer;
import org.acid.updater.other.Utilities;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Brandon on 2014-12-06.
 */
public class ClassInfo {
    private static LinkedHashMap<String, String> simbaMap;
    private final String id;
    private final String name;
    private final String parent;
    private final String interfaces;
    private final ArrayList<ClassField> fields;


    public ClassInfo(String id, ClassNode node) {
        this.id = id;
        this.name = node.name;
        this.parent = node.superName;
        this.interfaces = String.join(",", node.interfaces);
        this.fields = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public String getInterfaces() {
        return interfaces;
    }

    public void setField(ClassField field) {
        int i = 0;
        for (; i < fields.size(); ++i) {
            if (fields.get(i).getId().equalsIgnoreCase(field.getId())) {
                fields.remove(i);
                break;
            }
        }

        fields.add(i, field);
    }

    public void putField(ClassField field) {
        int i = 0;
        for (i = 0; i < fields.size(); ++i) {
            if (fields.get(i).getId().equalsIgnoreCase(field.getId())) {
                fields.remove(i);
                break;
            }
        }

        for (i = 0; i < fields.size(); ++i) {
            if (fields.get(i).getId().contains("*")) {
                break;
            }
        }

        if (field.getId().contains("*")) {
            fields.add(field);
        } else {
            fields.add(i, field);
        }
    }

    public ClassField getField(String id) {
        for (ClassField field : fields) {
            if (field.getId().equalsIgnoreCase(id)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String result = Utilities.padLeft(" ", 4) + Utilities.padMiddle(this.id + ":", this.name + "\n", 25);
        result += Utilities.padLeft("\n", 34, '-');
        for (ClassField field : fields) {
            result += field + "\n";
        }

        return result + "\n";
    }

    public String toNativeHeaderString() {
        String result = "/**  " + this.getId() + "  **/\n";
        result += "extern Hook HOOK_" + this.getId().toUpperCase() + "_CLASS;\n";

        for (ClassField field : fields) {
            if (!field.getId().contains("*")) {
                result += "extern Hook HOOK_" + this.getId().toUpperCase() + "_" + field.getId().toUpperCase() + ";";
                result += "\n";
            }
        }

        return result + "\n";
    }

    public String toNativeString() {
        String result = "/**  " + this.getId() + "  **/\n";
        Function<String, String> QuoteString = (String stringToQuote) -> "\"" + stringToQuote + "\"";

        result += "Hook HOOK_" + this.getId().toUpperCase() + "_CLASS";
        result += " = {" + QuoteString.apply(this.getName()) + "};\n";

        for (ClassField field : fields) {
            if (!field.getId().contains("*")) {
                result += "Hook HOOK_" + this.getId().toUpperCase() + "_" + field.getId().toUpperCase();

                String owner = field.getOwner() != null ? field.getOwner() : this.getName();

                if (field.getMultiplier() != 0) {
                    if (field.getDesc() != null && field.getDesc().length() > 0) {
                        result += " = {" + QuoteString.apply(owner) + ", " + QuoteString.apply(field.getName()) + ", " + QuoteString.apply(field.getDesc()) + ", " + field.getMultiplier() + "};";
                    } else {
                        result += " = {" + QuoteString.apply(owner) + ", " + QuoteString.apply(field.getName()) + ", " + field.getMultiplier() + "};";
                    }
                } else {
                    if (field.getDesc() != null && field.getDesc().length() > 0) {
                        result += " = {" + QuoteString.apply(owner) + ", " + QuoteString.apply(field.getName()) + ", " + QuoteString.apply(field.getDesc()) + "};";
                    } else {
                        result += " = {" + QuoteString.apply(owner) + ", " + QuoteString.apply(field.getName()) + "};";
                    }
                }
                result += "\n";
            }
        }

        return result + "\n";
    }

    public String toSimbaString() {
        LinkedHashMap<String, String> simbaMap = this.buildSimbaMap();
        String result = "{" + simbaMap.getOrDefault(this.getId(), this.getId()) + ": " + this.getName() + "}\n";

        for (ClassField field : fields) {
            if (!field.getId().contains("*")) {
                long multi = field.getMultiplier();
                String beg = this.getId() + "_" + field.getId();
                beg = simbaMap.getOrDefault(beg, beg) + ": THook = ";

                String mid = "['" + (field.getOwner() != null ? field.getOwner() + "." + field.getName() : field.getName()) + "', " + (multi != 0 ? multi : 1) + "];";
                result += Utilities.padRight(beg, 50) + mid + "\n";
            }
        }

        return result;
    }

    public String toSimbaNativeString() {
        LinkedHashMap<String, String> simbaMap = this.buildSimbaMap();
        String result = "{" + simbaMap.getOrDefault(this.getId(), this.getId()) + ": " + this.getName() + "}\n";

        for (ClassField field : fields) {
            if (!field.getId().contains("*")) {
                long multi = field.getMultiplier();
                String beg = this.getId() + "_" + field.getId();
                beg = simbaMap.getOrDefault(beg, beg) + ": THook = ";

                String owner = field.getOwner() != null ? field.getOwner() : this.getName();
                String mid = "['" + owner + "', '" + field.getName() + "', '" + field.getDesc() + "', " + (multi != 0 ? multi : 1) + "];";
                result += Utilities.padRight(beg, 50) + mid + "\n";
            }
        }

        return result;
    }

    public String toJSONString() {
        Function<ClassField, String> formatField = (f) -> {
            if (f.getOwner() != null) {
                return String.format("""
                                {
                                        "name": "%s",
                                        "cls": "%s",
                                        "field": "%s",
                                        "desc": "%s",
                                        "multiplier": %d
                                    }""",
                        f.getId(),
                        f.getOwner(),
                        f.getName(),
                        f.getDesc(),
                        f.getMultiplier() != 0 ? f.getMultiplier() : 1);
            }
            return String.format("""
                            {
                                    "name": "%s",
                                    "cls": null,
                                    "field": "%s",
                                    "desc": "%s",
                                    "multiplier": %d
                                }""",
                    f.getId(),
                    f.getName(),
                    f.getDesc(),
                    f.getMultiplier() != 0 ? f.getMultiplier() : 1);
        };

        String fields = "";
        for (ClassField f : this.fields) {
            if (f != this.fields.getFirst()) {
                fields += "    ";
            }

            fields += formatField.apply(f);

            if (f != this.fields.getLast()) {
                fields += ",\n";
            }
        }

        String result = String.format("""
                        {
                            "name": "%s",
                            "cls": "%s",
                            "parent": "%s",
                            "interfaces": "%s",
                            "fields": [%s]
                        }""",
                this.getId(),
                this.getName(),
                this.getParent(),
                this.getInterfaces(),
                fields);

        return result;
    }

    public String toPythonString(List<ClassInfo> allClasses) {
        String fields = "";
        for (ClassField f : this.fields) {
            if (!f.getId().contains("*")) {
                Function<String, String> toSnakeCase = s -> s
                        .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                        .replaceAll("\\s+", "_")
                        .toLowerCase()
                        .replaceFirst("^_", "");

                String name = toSnakeCase.apply(f.getId());

                String hook = "";
                long array_count = f.getDesc().chars().filter(ch -> ch == '[').count();

                if (f.getOwner() != null && !f.getOwner().equals(this.getName()) || this.getName().equals("client")) {
                    hook = "Hooks.%s.%s.cls, Hooks.%s.%s.field, Hooks.%s.%s.desc".formatted(this.getId(), f.getId(), this.getId(), f.getId(), this.getId(), f.getId());
                } else {
                    hook = "Hooks.%s.cls, Hooks.%s.%s.field, Hooks.%s.%s.desc".formatted(this.getId(), this.getId(), f.getId(), this.getId(), f.getId());
                }

                String def = "";
                if (f.getDesc().startsWith("Ljava/lang/String;")) {
                    def += "self.ref.reflect_string(%s)".formatted(hook);
                } else if (f.getDesc().startsWith("L")) {
                    def += "self.ref.reflect_object(%s)".formatted(hook);
                } else if (f.getDesc().contains("[Ljava/lang/String;")) {
                    def += "self.ref.reflect_array(%s).get_%dd(ReflectType.STRING)".formatted(hook, array_count);
                } else if (f.getDesc().contains("[L")) {
                    def += "self.ref.reflect_array(%s).get_%dd(ReflectType.OBJECT)".formatted(hook, array_count);
                } else if (f.getDesc().contains("[I")) {
                    def += "self.ref.reflect_array(%s).get_%dd(ReflectType.INT)".formatted(hook, array_count);
                } else if (f.getDesc().contains("[J")) {
                    def += "self.ref.reflect_array(%s).get_%dd(ReflectType.LONG)".formatted(hook, array_count);
                } else if (f.getDesc().contains("[C")) {
                    def += "self.ref.reflect_array(%s).get_%dd(ReflectType.CHAR)".formatted(hook, array_count);
                } else if (f.getDesc().contains("[F")) {
                    def += "self.ref.reflect_array(%s).get_%dd(ReflectType.FLOAT)".formatted(hook, array_count);
                } else if (f.getDesc().contains("[D")) {
                    def += "self.ref.reflect_array(%s).get_%dd(ReflectType.DOUBLE)".formatted(hook, array_count);
                } else if (f.getDesc().contains("[B")) {
                    def += "self.ref.reflect_array(%s).get_%dd(ReflectType.BYTE)".formatted(hook, array_count);
                } else if (f.getDesc().contains("[Z")) {
                    def += "self.ref.reflect_array(%s).get_%dd(ReflectType.BOOL)".formatted(hook, array_count);
                } else if (f.getDesc().startsWith("I")) {
                    if (f.getMultiplier() == 0 || f.getMultiplier() == 1) {
                        def += "self.ref.reflect_int(%s)".formatted(hook);
                    } else {
                        def += "(self.ref.reflect_int(%s) * Hooks.%s.%s.multiplier) & 0xFFFFFFFF".formatted(hook, this.getId(), f.getId());
                    }
                } else if (f.getDesc().startsWith("J")) {
                    if (f.getMultiplier() == 0 || f.getMultiplier() == 1) {
                        def += "self.ref.reflect_long(%s)".formatted(hook);
                    } else {
                        def += "(self.ref.reflect_long(%s) * Hooks.%s.%s.multiplier) & 0xFFFFFFFFFFFFFFFF".formatted(hook, this.getId(), f.getId());
                    }
                } else if (f.getDesc().startsWith("F")) {
                    def += "self.ref.reflect_float(%s)".formatted(hook);
                } else if (f.getDesc().startsWith("D")) {
                    def += "self.ref.reflect_double(%s)".formatted(hook);
                } else if (f.getDesc().startsWith("C")) {
                    def += "self.ref.reflect_char(%s)".formatted(hook);
                } else if (f.getDesc().startsWith("B")) {
                    def += "self.ref.reflect_byte(%s)".formatted(hook);
                } else if (f.getDesc().startsWith("Z")) {
                    def += "self.ref.reflect_bool(%s)".formatted(hook);
                } else if (f.getDesc().equals("N/A")) {
                    def += "None";
                } else {
                    System.err.println("STARTS WITH: " + f.getDesc());
                    System.exit(1);
                }

                String fieldType;
                String returnType = "";
                List<ClassInfo> found = allClasses.stream().filter(c -> {
                    if (f.getDesc().equals("N/A")) {
                        return false;
                    }

                    Type type = Type.getType(f.getDesc());
                    if (type.getDescriptor().startsWith("[") && type.getDimensions() >= 1) {
                        return c.getName().equals(type.getElementType().getClassName());
                    }

                    return c.getName().equals(type.getClassName());
                }).toList();

                if (!found.isEmpty()) {
                    fieldType = found.getFirst().getId();

                    if (!f.getDesc().startsWith("Ljava/lang/String;") && f.getDesc().startsWith("L")) {
                        def = "%s(%s)".formatted(fieldType, def);
                    }
                } else {
                    fieldType = "";
                }

                if (f.getDesc().equals("N/A")) {
                    returnType = "None";
                } else {
                    int dimensions = 0;

                    Type type = Type.getType(f.getDesc());
                    if (type.getDescriptor().startsWith("[") && type.getDimensions() >= 1) {
                        dimensions = type.getDimensions();
                    }

                    for (int i = 0; i < dimensions; ++i) {
                        returnType += "List[";
                    }

                    Function<String, String> mapType = (t) -> {
                        switch (t) {
                            case "boolean":
                                return "bool";
                            case "char":
                                return "str";
                            case "byte":
                                return "int";
                            case "int":
                                return "int";
                            case "long":
                                return "int";
                            case "float":
                                return "float";
                            case "double":
                                return "float";
                            case "java.lang.String":
                                return "str";
                            case "java.lang.Map":
                                return "JavaObject";
                        }
                        return fieldType;
                    };

                    if (!fieldType.isEmpty()) {
                        returnType += fieldType;
                    } else if (dimensions > 0) {
                        returnType += mapType.apply(type.getElementType().getClassName());
                    } else {
                        returnType += mapType.apply(type.getClassName());
                    }

                    for (int i = 0; i < dimensions; ++i) {
                        returnType += "]";
                    }
                }

                if (f.getOwner() != null && !f.getOwner().equals(this.getName()) || this.getName().equals("client")) {
                    fields += "    @classproperty\n    def %s(self) -> %s:\n        return ".formatted(name, returnType);
                } else {
                    fields += "    @property\n    def %s(self) -> %s:\n        return ".formatted(name, returnType);
                }

                fields += "%s\n\n".formatted(def);
            }
        }

        String cls = """
                from remote_input import ReflectType
                from Reflection.Internal import Hooks
                from Reflection import JavaObject
                
                
                class %s(JavaObject):
                %s
                """.formatted(this.getId(), fields);
        return cls;
    }

    private String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    private String camelCase(String line) {
        if (line.length() > 3) {
            return Character.toLowerCase(line.charAt(0)) + line.substring(1);
        }
        return line;
    }

    public void refactor(Collection<ClassNode> classes) {
        Renamer r = new Renamer(classes);

        fields.forEach(f -> {
            if (!f.getId().contains("*")) {
                if (f.getName().contains(".")) {
                    String c = f.getName().substring(0, f.getName().indexOf('.'));
                    String n = f.getName().substring(f.getName().indexOf('.') + 1);
                    r.renameField(c, n, f.getDesc(), camelCase(f.getId()), null);
                    r.renameField(capitalize(c), n, f.getDesc(), camelCase(f.getId()), null);
                } else {
                    r.renameField(this.getName(), f.getName(), f.getDesc(), camelCase(f.getId()), null);
                }
            } else {
                if (f.getName().length() < 3) {
                    r.renameMethod(this.getName(), f.getName(), f.getDesc(), camelCase(f.getId().replace("*", "")), null);
                }
            }
        });

        r.renameClass(this.getName(), this.getId());
    }

    private LinkedHashMap<String, String> buildSimbaMap() {
        if (simbaMap == null || simbaMap.size() == 0) {
            simbaMap = new LinkedHashMap<>();

            simbaMap.put("Widget_Items", "Widget_ItemIDs");
            simbaMap.put("Widget_ItemStackSizes", "Widget_StackSizes");

            simbaMap.put("HashTable_Capacity", "HashTable_Size");

            simbaMap.put("IterableHashTable_Capacity", "IterableHashTable_Size");

            simbaMap.put("Client_GameCycle", "Client_LoopCycle");


            simbaMap.put("Client_WidgetSettings", "Client_GameSettings");


            simbaMap.put("Client_CrosshairColour", "Client_CrossHairColor");

            simbaMap.put("Client_CosineTable", "Client_Cosine");
            simbaMap.put("Client_SineTable", "Client_Sine");
            simbaMap.put("Client_EnergyLevel", "Client_Energy");
            simbaMap.put("Client_PlayerWeight", "Client_Weight");

            simbaMap.put("Interactable_RelativeX", "GameObject_WorldX");
            simbaMap.put("Interactable_RelativeY", "GameObject_WorldY");
            simbaMap.put("Interactable_SizeX", "GameObject_OffsetX");
            simbaMap.put("Interactable_SizeY", "GameObject_OffsetY");

            simbaMap.put("Item_Quantity", "Item_StackSizes");
        }
        return simbaMap;
    }
}
