package com.Nemuriciu.Swordfall;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.ThreadLocalRandom;

class CombatInfo {
    private final String[] ATK_MSG = {" attacks for ", " hits for ", " deals "};

    private Context context;
    private ThreadLocalRandom r;

    boolean playerTurn;
    int playerLevel, enemyLevel;
    int enemyHp, enemyAtk, enemyDef, enemyMinDmg, enemyMaxDmg, enemyLuck;
    int enemyXp, enemyGold;
    String playerName, enemyName;
    String playerAvatar, enemyAvatar;

    TextView playerHealthText, enemyHealthText;
    TextView playerHitText, enemyHitText;
    ProgressBar playerHealth;
    ProgressBar enemyHealth;
    LinearLayout log;
    ScrollView scroll;


    CombatInfo(Context context) {
        this.context = context;
        this.r = ThreadLocalRandom.current();
    }

    void setEnemy(Creature c) {
        this.enemyName = c.name;
        this.enemyAvatar = c.avatar;

        ThreadLocalRandom r = ThreadLocalRandom.current();

        enemyHp = r.nextInt(c.minHp, c.maxHp);
        enemyAtk = r.nextInt(c.minAtk, c.maxAtk);
        enemyDef = r.nextInt(c.minDef, c.maxDef);
        enemyMinDmg = c.minDmg;
        enemyMaxDmg = c.maxDmg;
        enemyLuck = r.nextInt(c.minLuck, c.maxLuck);
        enemyXp = r.nextInt(c.minExp, c.maxExp);
        enemyGold = r.nextInt(c.minGold, c.maxGold);
    }

    @SuppressLint("SetTextI18n")
    void dealDamage(String target, long value) {
        // TODO: Check for critical
        boolean crit = false;
        if (r.nextDouble() <= 0.1) {
            crit = true;
            value *= 1.5;
        }

        if (target.equals("Enemy")) {
            long hp = enemyHealth.getProgress();
            long res = (hp - value <= 0) ? 0 : hp - value;

            enemyHealth.setProgress((int)res);
            enemyHealthText.setText(enemyHealth.getProgress() + "/" + enemyHealth.getMax());

            int ix = r.nextInt(0, ATK_MSG.length);
            String logMsg = playerName + ATK_MSG[ix] + value + " dmg.";
            int color = Color.WHITE;

            if (crit) {
                logMsg += " (Crit)";
                color = Color.YELLOW;
            }

            addLog(logMsg, color);
            enemyHitText.setTextColor(color);
            enemyHitText.setText(String.valueOf(value));

            AlphaAnimation alphaAnim = new AlphaAnimation(1.0f,0.0f);
            alphaAnim.setStartOffset(500);
            alphaAnim.setDuration(100);
            alphaAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                public void onAnimationEnd(Animation animation) {
                    enemyHitText.setText("");

                    // TODO: Stun
                    if (enemyHealth.getProgress() > 0)
                        enemyAttack();
                    else {
                        addLog(playerName + " wins!", Color.GREEN);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            enemyHitText.setAnimation(alphaAnim);

        } else if (target.equals("Player")) {
            long hp = playerHealth.getProgress();
            long res = (hp - value <= 0) ? 0 : hp - value;

            playerHealth.setProgress((int)res);
            playerHealthText.setText(playerHealth.getProgress() + "/" + playerHealth.getMax());

            int ix = r.nextInt(0, ATK_MSG.length);
            String logMsg = enemyName + ATK_MSG[ix] + value + " dmg.";
            int color = Color.WHITE;

            if (crit) {
                logMsg += " (Crit)";
                color = Color.YELLOW;
            }

            addLog(logMsg, color);
            playerHitText.setTextColor(color);
            playerHitText.setText(String.valueOf(value));

            AlphaAnimation alphaAnim = new AlphaAnimation(1.0f,0.0f);
            alphaAnim.setStartOffset(500);
            alphaAnim.setDuration(100);
            alphaAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                public void onAnimationEnd(Animation animation) {
                    playerHitText.setText("");

                    // TODO: Stun
                    if (playerHealth.getProgress() > 0)
                        playerTurn = true;
                    else {
                        addLog(enemyName + " wins!", Color.GREEN);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            playerHitText.setAnimation(alphaAnim);
        }
    }

    private void enemyAttack() {
        // Delay //
        try {
            Thread.sleep(300);
        } catch (Exception e){
            e.printStackTrace();
        }

        long dmg = r.nextLong(enemyMinDmg, enemyMaxDmg);
        dealDamage("Player", dmg);
    }

    private void addLog(String msg, int color) {
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 6, 4, 0);
        tv.setLayoutParams(params);
        tv.setText(msg);
        tv.setTextSize(10);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(color);
        log.addView(tv);

        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }
}
