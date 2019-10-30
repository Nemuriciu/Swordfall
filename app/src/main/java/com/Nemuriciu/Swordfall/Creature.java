package com.Nemuriciu.Swordfall;

class Creature {

    private int id;
    int level;
    int minHp, maxHp;
    int minAtk, maxAtk;
    int minDef, maxDef;
    int minDmg, maxDmg;
    int minLuck, maxLuck;
    int minExp, maxExp;
    int minGold, maxGold;
    String name, avatar;

    Creature(int id, int level, String name, String avatar,
             int minHp, int maxHp, int minAtk, int maxAtk,
             int minDef, int maxDef, int minDmg, int maxDmg,
             int minLuck, int maxLuck, int minExp, int maxExp,
             int minGold, int maxGold) {
        this.id = id;
        this.level = level;
        this.name = name;
        this.avatar = avatar;
        this.minHp = minHp;
        this.maxHp = maxHp;
        this.minAtk = minAtk;
        this.maxAtk = maxAtk;
        this.minDef = minDef;
        this.maxDef = maxDef;
        this.minDmg = minDmg;
        this.maxDmg = maxDmg;
        this.minLuck = minLuck;
        this.maxLuck = maxLuck;
        this.minExp = minExp;
        this.maxExp = maxExp;
        this.minGold = minGold;
        this.maxGold = maxGold;
    }
}
