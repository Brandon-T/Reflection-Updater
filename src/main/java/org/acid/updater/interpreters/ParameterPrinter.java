package org.acid.updater.interpreters;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class ParameterPrinter {
    void print(MethodNode m) {
        boolean isStatic = (m.access & Opcodes.ACC_STATIC) != 0;

        int parameterSlots = 0;
        if (!isStatic) {
            parameterSlots++;
        }

        Type[] parameterTypes = Type.getArgumentTypes(m.desc);
        for (Type parameterType : parameterTypes) {
            parameterSlots += parameterType.getSize();
        }
        String[] slotNames = new String[parameterSlots];

        for (int i = 0; i < slotNames.length; ++i) {
            slotNames[i] = "var" + (isStatic ? i + 1 : i);
        }

        int slot = 0;
        if (!isStatic) {
            slot++;
        }

        List<String> result = new ArrayList<>();
        for (Type parameterType : parameterTypes) {
            String slotName = slotNames[slot];
            if (slotName == null) {
                break;
            }
            result.add(slotName);

            slot += parameterType.getSize();
        }

        for (String p : result) {
            System.out.println(p);
        }
    }

}
