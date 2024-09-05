package acid.deobfuscator;

import jdk.internal.org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Kira on 2015-01-08.
 */
public class TryCatchRemover extends Deobfuscator {
    private int try_count = 0, removal_count = 0;

    public TryCatchRemover(Collection<ClassNode> classes) {
        super(classes);
    }

    public TryCatchRemover analyse() {
        classes.stream().forEach(n -> n.methods.forEach(m -> {
            if (m.tryCatchBlocks != null) {
                try_count += m.tryCatchBlocks.size();
            }
        }));
        return this;
    }

    public void remove() {
        classes.stream().forEach(n -> n.methods.forEach(this::removeEmptyTryCatch));
        System.out.println("Removed TryCatchBlocks: " + removal_count + " of " + try_count);
    }

    private void removeEmptyTryCatch(MethodNode method) {
        if (method.tryCatchBlocks != null) {
            for (int i = 0; i < method.tryCatchBlocks.size(); ++i) {
                int si = method.instructions.indexOf(method.tryCatchBlocks.get(i).start);
                int se = method.instructions.indexOf(method.tryCatchBlocks.get(i).end);
                int sh = method.instructions.indexOf(method.tryCatchBlocks.get(i).handler);

                if (sh - si > 0 && sh - si < 4) {
                    //method.tryCatchBlocks.remove(method.tryCatchBlocks.get(i)); --i;
                    i = removeTryCatch(method, null, si, se, sh, i);
                    ++removal_count;
                }
            }
        }
    }

    private boolean canRemoveNonThrowingTryCatch(MethodInsnNode m, int i) {
        for (ClassNode c : classes) {
            if (c.name.equals(m.owner)) {
                for (MethodNode mn : c.methods) {
                    if (mn.name.equals(m.name) && mn.desc.equals(m.desc) && mn.exceptions.isEmpty()) {
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }

    private void removeNonThrowingTryCatch(MethodNode method) {
        HashMap<LabelNode, Integer> shared_starts = new HashMap<>();
        if (method.tryCatchBlocks != null) {
            method.tryCatchBlocks.forEach(t -> {
                if (!shared_starts.containsKey(t.start)) {
                    shared_starts.put(t.start, 0);
                } else {
                    shared_starts.put(t.start, shared_starts.get(t.start) + 1);
                }
            });
        }

        if (method.tryCatchBlocks != null) {
            for (int i = 0; i < method.tryCatchBlocks.size(); ++i) {
                int si = method.instructions.indexOf(method.tryCatchBlocks.get(i).start);
                int sh = method.instructions.indexOf(method.tryCatchBlocks.get(i).handler);
                int se = method.instructions.indexOf(method.tryCatchBlocks.get(i).end);

                if (se - si > 0) {
                    boolean should_remove = true;

                    for (int j = si + 1; j < se; ++j) {
                        //check to see if any MethodInsnNodes can throw..
                        if (method.instructions.get(j) instanceof MethodInsnNode) {
                            MethodInsnNode m = (MethodInsnNode)method.instructions.get(j);
                            if (m.owner.contains("Exception") || !canRemoveNonThrowingTryCatch(m, i)) {
                                should_remove = false;
                            }
                        }
                    }

                    if (should_remove) {
                        i = removeTryCatch(method, shared_starts, si, se, sh, i);
                    }
                }
            }
        }
    }

    private int removeTryCatch(MethodNode method, HashMap<LabelNode, Integer> shared_starts, int si, int se, int sh, int i) {
        int goto_index = se + 1;
        if (method.instructions.get(goto_index) instanceof JumpInsnNode) {
            int catch_end_index = method.instructions.indexOf(((JumpInsnNode) method.instructions.get(goto_index)).label);

            for (int k = catch_end_index - 1; k > goto_index; --k) {
                method.instructions.remove(method.instructions.get(k)); //delete all code in the catch block.
            }

            method.instructions.remove(method.instructions.get(sh)); //delete goto block.
            method.instructions.remove(method.instructions.get(goto_index)); //delete the goto statement.
            method.instructions.remove(method.instructions.get(se)); //delete frame.

            if (shared_starts != null && shared_starts.containsKey(method.tryCatchBlocks.get(i).start)) {
                int value = shared_starts.get(method.tryCatchBlocks.get(i).start);
                if (value == 0) {
                    method.instructions.remove(method.instructions.get(si)); //delete first try-catch-label.
                    shared_starts.remove(method.tryCatchBlocks.get(i).start);
                } else {
                    shared_starts.put(method.tryCatchBlocks.get(i).start, value - 1);
                }
            } else {
                method.instructions.remove(method.instructions.get(si)); //delete first try-catch-label.
            }

            method.tryCatchBlocks.remove(i);
            return --i;
        }
        return i;
    }
}
