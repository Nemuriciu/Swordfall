package com.Nemuriciu.Swordfall;

import java.util.ArrayList;
import java.util.Random;

class CombatManager {
    private String pName, eName;
    private long pLvl, eLvl;

    private long pHp, eHp;
    private long pAtk, eAtk, pDef, eDef;
    private long pDmgMin, pDmgMax, eDmgMin, eDmgMax, pLuck, eLuck;

    private ArrayList<String> combatLog;
    private boolean pTurn = true;
    private int currentRound;

    CombatManager(String pName, String eName, long pLvl, long eLvl,
                  long pHp, long eHp, long pAtk, long eAtk, long pDef,
                  long eDef, long pDmgMin, long pDmgMax, long eDmgMin,
                  long eDmgMax, long pLuck, long eLuck) {
        this.pName = pName;
        this.eName = eName;
        this.pLvl = pLvl;
        this.eLvl = eLvl;
        this.pHp = pHp;
        this.eHp = eHp;
        this.pAtk = pAtk;
        this.eAtk = eAtk;
        this.pDef = pDef;
        this.eDef = eDef;
        this.pDmgMin = pDmgMin;
        this.pDmgMax = pDmgMax;
        this.eDmgMin = eDmgMin;
        this.eDmgMax = eDmgMax;
        this.pLuck = pLuck;
        this.eLuck = eLuck;
        combatLog = new ArrayList<>();
    }

    private double getHitChance(long pLvl, long eLvl, long atk, long def) {
        double difFactor = (atk - def) / (double)Math.max(pLvl, eLvl) * 0.02;
        double lvlFactor = 0.75 + Math.max(pLvl, eLvl) * 0.002;

        double hitChance = (double)atk / (double)def + difFactor * lvlFactor;

        // Hit Chance bounds (5%, 98%) //
        if (hitChance < 0.05)
            hitChance = 0.05;
        else if (hitChance > 0.98)
            hitChance = 0.98;

        return hitChance;
    }

    long getPlayerHp() {
        return pHp;
    }

    long getEnemyHp() {
        return eHp;
    }

    ArrayList<String> getCombatLog() {
        return combatLog;
    }

    String simulateCombat() {
        Random r = new Random();
        String result = "Inconclusive";
        double pHitChance = getHitChance(pLvl, eLvl, pAtk, eDef);
        double eHitChance = getHitChance(eLvl, pLvl, eAtk, pDef);
        //Log.e(TAG, "P_HIT: " + pHitChance);
        //Log.e(TAG, "E_HIT: " + eHitChance);

        while (pHp > 0 && eHp > 0) {
            if (currentRound == 40)                                // CombatInfo inconclusive after 40 rounds
                break;

            if (pTurn) {
                if (r.nextDouble() <= pHitChance) {                 // Player Hits Enemy
                    // TODO: Calculate Luck
                    int dmg = r.nextInt((int)pDmgMax - (int)pDmgMin) + (int)pDmgMin;
                    String log = pName + " hits for " + dmg + " damage.";
                    combatLog.add(log);
                    eHp -= dmg;
                } else {                                            // Player Miss Enemy
                    String log = pName + " miss the attack.";
                    combatLog.add(log);
                }
            } else {
                if (r.nextDouble() <= eHitChance) {                 // Enemy Hits Player
                    // TODO: Calculate Luck
                    int dmg = r.nextInt((int)eDmgMax - (int)eDmgMin) + (int)eDmgMin;
                    String log = eName + " hits for " + dmg + " damage.";
                    combatLog.add(log);
                    pHp -= dmg;
                } else {                                            // Enemy Miss Player
                    String log = eName + " miss the attack.";
                    combatLog.add(log);
                }
            }

            pTurn = !pTurn;
            currentRound++;
        }

        if (eHp <= 0) {
            result = "Victory";
            eHp = 0;
        }
        else if (pHp <= 0) {
            result = "Defeat";
            pHp = 0;
        }

        return result;
    }
}
