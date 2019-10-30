package com.Nemuriciu.Swordfall;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

class CombatManager {
    private static final String TAG = "CombatManager";
    private String pName, eName;
    private long pLvl, eLvl;

    private long pHp, eHp;
    private long pAtk, eAtk, pDef, eDef, pDmg, eDmg, pLuck, eLuck;

    private ArrayList<String> combatLog;
    private boolean pTurn = true;
    private int currentRound;

    CombatManager(String pName, String eName, long pLvl, long eLvl,
                  long pHp, long eHp, long pAtk, long eAtk, long pDef,
                  long eDef, long pDmg, long eDmg, long pLuck, long eLuck) {
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
        this.pDmg = pDmg;
        this.eDmg = eDmg;
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
        Log.e(TAG, "P_HIT: " + pHitChance);
        Log.e(TAG, "E_HIT: " + eHitChance);

        while (pHp > 0 && eHp > 0) {
            if (currentRound == 40)                                // Combat inconclusive after 100 rounds
                break;

            if (pTurn) {
                if (r.nextDouble() <= pHitChance) {                 // Player Hits Enemy
                    // TODO: Calculate Luck
                    String log = pName + " hits " + eName + " for " + pDmg + " Dmg";
                    combatLog.add(log);
                    eHp -= pDmg;
                } else {                                            // Player Miss Enemy
                    String log = pName + " miss the attack on " + eName;
                    combatLog.add(log);
                }
            } else {
                if (r.nextDouble() <= eHitChance) {                 // Enemy Hits Player
                    // TODO: Calculate Luck
                    String log = eName + " hits " + pName + " for " + eDmg + " Dmg";
                    combatLog.add(log);
                    pHp -= eDmg;
                } else {                                            // Enemy Miss Player
                    String log = eName + " miss the attack on " + pName;
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
