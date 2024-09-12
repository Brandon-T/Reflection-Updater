package org.acid.updater.structures;

import org.acid.updater.other.Utilities;

/**
 * Created by Brandon on 2014-12-06.
 */
public class ClassField {
    private String id;
    private String owner;
    private String name;
    private String desc;
    private long multiplier;

    public ClassField(String id) {
        this(id, null, null, null);
    }

    public ClassField(String id, String name, String desc) {
        this(id, null, name, desc);
    }

    public ClassField(String id, String owner, String name, String desc) {
        this(id, owner, name, desc, 0);
    }

    public ClassField(String id, String name, String desc, long multiplier) {
        this(id, null, name, desc, multiplier);
    }

    public ClassField(String id, String owner, String name, String desc, long multiplier) {
        this.id = id;
        this.owner = owner;
        this.name = name == null ? "N/A" : name;
        this.desc = desc == null ? "N/A" : desc;
        this.multiplier = multiplier;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(long multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String toString() {
        if (multiplier == 0) {
            return "    " + Utilities.padRight(id, 20) + " ->   " + (owner != null ? owner + "." + name : name);
        }
        return "    " + Utilities.padRight(id, 20) + " ->   " + Utilities.padRight(owner != null ? owner + "." + name : name, 5) + " *  " + multiplier;
    }
}
