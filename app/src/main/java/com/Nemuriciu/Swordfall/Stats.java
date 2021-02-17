package com.Nemuriciu.Swordfall;

import java.io.Serializable;

public class Stats implements Serializable, Cloneable {

    public Long atk, def, vit, luck;
    public Long dmgMin, dmgMax;
    public Long unspentPts;

    public Stats() {}

    public Stats(long atk, long def, long vit, long luck, long dmgMin, long dmgMax, long unspentPts) {
        this.atk = atk;
        this.def = def;
        this.vit = vit;
        this.luck = luck;
        this.dmgMin = dmgMin;
        this.dmgMax = dmgMax;
        this.unspentPts = unspentPts;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "atk=" + atk +
                ", def=" + def +
                ", vit=" + vit +
                ", luck=" + luck +
                ", dmgMin=" + dmgMin +
                ", dmgMax=" + dmgMax +
                ", unspentPts=" + unspentPts +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
