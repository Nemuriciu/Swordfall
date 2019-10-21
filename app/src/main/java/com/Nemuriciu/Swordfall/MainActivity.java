package com.Nemuriciu.Swordfall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String uid;
    private String username, classs;
    private long level, gold, currentStam, maxStam;
    private long currentExp, maxExp;
    private long classColor;

    private TextView usernameText, levelText, classText;
    private TextView goldText, staminaText;
    private ProgressBar expBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameText = findViewById(R.id.username);
        levelText = findViewById(R.id.level);
        classText = findViewById(R.id.class_);
        goldText = findViewById(R.id.infoGold);
        staminaText = findViewById(R.id.infoStamina);
        expBar = findViewById(R.id.expBar);
        Drawable progressDrawable = new ProgressDrawable(
                Color.parseColor("#EA7500"), Color.parseColor("#442200"));
        expBar.setProgressDrawable(progressDrawable);

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        getExtras(extras);
        updateUI();

        setMenuButtons();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> MainActivity.super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
    }

    private void getExtras(Bundle extras) {
        uid = extras.getString("uid");
        username = extras.getString("username");
        classs = extras.getString("class");
        level = extras.getLong("level");
        gold = extras.getLong("gold");
        currentStam = extras.getLong("currentStamina");
        maxStam = extras.getLong("maxStamina");
        currentExp = extras.getLong("currentExp");
        maxExp = extras.getLong("maxExp");
    }

    @SuppressLint("SetTextI18n")
    public void updateUI() {
        switch (classs) {
            case "FIGHTER":
                classColor = Color.parseColor("#FFC107");
                break;
            case "RANGER":
                classColor = Color.parseColor("#8BC34A");
                break;
            case "WIZARD":
                classColor = Color.parseColor("#2196F3");
                break;
        }

        String str = classs.toLowerCase();
        str = str.substring(0, 1).toUpperCase() + str.substring(1);

        usernameText.setText(username);
        levelText.setText("<" + level + ">");
        levelText.setTextColor((int)classColor);
        classText.setText(str);
        classText.setTextColor((int)classColor);
        goldText.setText(String.format(Locale.ENGLISH,"%,d", gold));
        staminaText.setText(String.format(Locale.ENGLISH,"%,d", currentStam) + "/"
                + String.format(Locale.ENGLISH,"%,d", maxStam));
        expBar.setMax((int)maxExp);
        expBar.setProgress((int)currentExp);
    }

    private void setMenuButtons() {
        ImageView combatButton = findViewById(R.id.menuItemCombat);
        ImageView settingsButton = findViewById(R.id.menuItemSettings);

        settingsButton.setOnClickListener(v -> {
            if (v.getId() == R.id.menuItemSettings) {
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(task -> {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        });
            }
        });

        combatButton.setOnClickListener(v -> {
            if (v.getId() == R.id.menuItemCombat) {
                Intent intent = new Intent(this, CombatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("uid", uid);
                intent.putExtra("username", username);
                intent.putExtra("class", classs);
                intent.putExtra("level", level);
                intent.putExtra("gold", gold);
                intent.putExtra("currentStamina", currentStam);
                intent.putExtra("maxStamina", maxStam);
                intent.putExtra("currentExp", currentExp);
                intent.putExtra("maxExp", maxExp);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}
