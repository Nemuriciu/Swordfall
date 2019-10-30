package com.Nemuriciu.Swordfall;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

import androidx.annotation.Nullable;

public class MobCard extends LinearLayout implements View.OnClickListener {

    private ImageView avatar;
    private TextView level, name;
    private TextView health;
    private TextView atkValue, defValue, dmgValue, luckValue;
    private Button attackButton;
    private int exp, gold;
    Creature mob;

    public MobCard(Context context, Creature c) {
        super(context);
        mob = c;
        init();
    }

    public MobCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MobCard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initialize view
     */
    private void init(){
        inflate(getContext(), R.layout.mob_card,this);

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 12,0,0 );
        setLayoutParams(params);
        setBackground(getResources().getDrawable(R.drawable.combat_mob_card_background));

        //Get references to views
        avatar = findViewById(R.id.mobCardAvatar);
        level = findViewById(R.id.mobCardLevel);
        name = findViewById(R.id.mobCardName);
        health = findViewById(R.id.mobCardHealth);
        atkValue = findViewById(R.id.mobCardAtk);
        defValue = findViewById(R.id.mobCardDef);
        dmgValue = findViewById(R.id.mobCardDmg);
        luckValue = findViewById(R.id.mobCardLuck);
        attackButton = findViewById(R.id.mobCardAttackButton);
        attackButton.setOnClickListener(this);

        updateCard();
    }

    @SuppressLint("SetTextI18n")
    private void updateCard() {
        int id = getResources().getIdentifier(mob.avatar, "drawable", "com.Nemuriciu.Swordfall");
        Drawable drawable = getResources().getDrawable(id);
        avatar.setImageDrawable(drawable);

        Random r = new Random();
        name.setText(mob.name);
        level.setText("Lv. " + mob.level);
        health.setText(String.valueOf(
                r.nextInt(mob.maxHp - mob.minHp + 1) + mob.minHp));
        atkValue.setText(String.valueOf(
                r.nextInt(mob.maxAtk - mob.minAtk + 1) + mob.minAtk));
        defValue.setText(String.valueOf(
                r.nextInt(mob.maxDef - mob.minDef + 1) + mob.minDef));
        dmgValue.setText(String.valueOf(
                r.nextInt(mob.maxDmg - mob.minDmg + 1) + mob.minDmg));
        luckValue.setText(String.valueOf(
                r.nextInt(mob.maxLuck - mob.minLuck + 1) + mob.minLuck));
        exp = r.nextInt(mob.maxExp- mob.minExp + 1) + mob.minExp;
        gold = r.nextInt(mob.maxGold- mob.minGold + 1) + mob.minGold;
    }

    public int getHp() {
        return Integer.parseInt(health.getText().toString());
    }

    public int getAtk() {
        return Integer.parseInt(atkValue.getText().toString());
    }

    public int getDef() {
        return Integer.parseInt(defValue.getText().toString());
    }

    public int getDmg() {
        return Integer.parseInt(dmgValue.getText().toString());
    }

    public int getLuck() {
        return Integer.parseInt(luckValue.getText().toString());
    }

    public int getExp() {
        return exp;
    }

    public int getGold() {
        return gold;
    }

    /**
     * MobCard Click Listeners
     */
    @Override
    public void onClick(View v) {
        if (v == attackButton){
            if (CombatActivity.attackLock) return;
            CombatActivity.attackLock = true;

            CombatActivity combatActivity = (CombatActivity)getContext();
            combatActivity.startCombat(this);
        }
    }
}
