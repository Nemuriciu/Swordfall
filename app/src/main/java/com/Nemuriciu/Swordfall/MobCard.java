package com.Nemuriciu.Swordfall;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MobCard extends LinearLayout implements View.OnClickListener {

    protected ImageView avatar;
    protected TextView level, name;
    protected TextView health;
    protected TextView atkValue, defValue, dmgValue, luckValue;
    private Button attackButton;

    public MobCard(Context context) {
        super(context);
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

    public MobCard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
        params.setMargins(0, 8,0,0 );
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
    }

    @SuppressLint("SetTextI18n")
    public void updateCard(String avatarRef, String name, long lvl, long hp,
                           long atk, long def, long dmg, long luck) {
        int id = getResources().getIdentifier(avatarRef, "drawable", "com.Nemuriciu.Swordfall");
        Drawable drawable = getResources().getDrawable(id);
        avatar.setImageDrawable(drawable);

        this.name.setText(name);
        this.level.setText("Lv. " + lvl);
        this.health.setText(String.valueOf(hp));
        this.atkValue.setText(String.valueOf(atk));
        this.defValue.setText(String.valueOf(def));
        this.dmgValue.setText(String.valueOf(dmg));
        this.luckValue.setText(String.valueOf(luck));
    }

    @Override
    public void onClick(View v) {
        if (v == attackButton){
            CombatActivity combatActivity = (CombatActivity)getContext();
            combatActivity.startCombat(this);
        }
    }
}
