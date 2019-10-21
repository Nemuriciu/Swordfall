package com.Nemuriciu.Swordfall;

public class Zone {

    private int minLevel, maxLevel;
    private long depth;

    public Zone(int minLevel, int maxLevel, long depth) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.depth = depth;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public long getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
