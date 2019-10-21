package com.Nemuriciu.Swordfall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class CombatActivity extends AppCompatActivity {

    private static final String TAG = "CombatActivity";

    private String uid;
    private String username, classs;
    private long level, gold, currentStam, maxStam;
    private long currentExp, maxExp;
    private long classColor;

    private TextView usernameText, levelText, classText;
    private TextView goldText, staminaText;
    private ProgressBar expBar;

    private View currentLayout;

    private FirebaseFirestore db;
    private List zoneDepth;
    private Zone zone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combat);
        db = FirebaseFirestore.getInstance();

        usernameText = findViewById(R.id.username);
        levelText = findViewById(R.id.level);
        classText = findViewById(R.id.class_);
        goldText = findViewById(R.id.infoGold);
        staminaText = findViewById(R.id.infoStamina);
        expBar = findViewById(R.id.expBar);
        Drawable progressDrawable = new ProgressDrawable(
                Color.parseColor("#EA7500"), Color.parseColor("#442200"));
        expBar.setProgressDrawable(progressDrawable);
        currentLayout = findViewById(R.id.zoneScrollView);

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        getExtras(extras);
        updateUI();
        setMenuButtons();
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
    private void updateUI() {
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
        ImageView profileButton = findViewById(R.id.menuItemProfile);
        ImageView settingsButton = findViewById(R.id.menuItemSettings);

        settingsButton.setOnClickListener(v -> {
            if (v.getId() == R.id.menuItemSettings) {
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(task -> {
                            startActivity(new Intent(CombatActivity.this, LoginActivity.class));
                            finish();
                        });
            }
        });

        profileButton.setOnClickListener(v -> {
            if (v.getId() == R.id.menuItemProfile) {
                Intent intent = new Intent(this, MainActivity.class);
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
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void updateZoneMobList() {
        TextView depthText = findViewById(R.id.zoneDepthLevel);
        LinearLayout mobList = findViewById(R.id.mobScrollList);
        mobList.removeAllViews();

        depthText.setText(String.valueOf(zone.getDepth()));

        for (int i = 0; i < 15; i++) {
            MobCard mobCard = new MobCard(this);

            if (i % 2 == 0)
                mobCard.updateCard("avatar_swamp_lizardman", "Lizardman",
                        new Random().nextInt(4) + 1,
                        new Random().nextInt(100) + 200,
                        new Random().nextInt(10) + 5,
                        new Random().nextInt(10) + 5,
                        new Random().nextInt(5) + 10,
                        new Random().nextInt(4) + 1);
            else
                mobCard.updateCard("avatar_swamp_snake", "Snake",
                        new Random().nextInt(4) + 1,
                        new Random().nextInt(100) + 200,
                        new Random().nextInt(10) + 5,
                        new Random().nextInt(10) + 5,
                        new Random().nextInt(5) + 10,
                        new Random().nextInt(4) + 1);
            mobList.addView(mobCard);
        }
    }

    @SuppressLint("SetTextI18n")
    public void startCombat(MobCard mobCard) {
        // Get Combat Page Views //
        ImageView playerAvatar = findViewById(R.id.mobCombatPlayerAvatar);
        ImageView mobAvatar = findViewById(R.id.mobCombatMobAvatar);
        TextView mobHpBarText = findViewById(R.id.mobCombatMobHpText);
        ProgressBar mobHpBar = findViewById(R.id.mobCombatMobHp);

        // Get values from Mob Card //
        int mobHp = Integer.parseInt(mobCard.health.getText().toString());

        // Set Player Avatar //
        Drawable d = findViewById(R.id.avatar).getBackground();
        playerAvatar.setImageDrawable(d);

        // Set Mob Avatar //
        mobAvatar.setImageDrawable(mobCard.avatar.getDrawable());

        // Set Mob Health //
        mobHpBar.setMax(mobHp);
        mobHpBar.setProgress(mobHp);
        mobHpBarText.setText(mobHpBar.getProgress() + "/" + mobHpBar.getMax());

        // Set Player Health // TODO:

        // Calculate combat result // TODO:

        currentLayout = findViewById(R.id.mobCombatLayout);
        currentLayout.setVisibility(View.VISIBLE);
    }

    public void exitZoneClickListener(View view) {
        if (view.getId() == R.id.mobListClose) {
            currentLayout.setVisibility(View.GONE);
            currentLayout = findViewById(R.id.zoneScrollView);
            currentLayout.setVisibility(View.VISIBLE);
        } else if (view.getId() == R.id.mobCombatClose) {
            currentLayout.setVisibility(View.GONE);
            currentLayout = findViewById(R.id.mobListLayout);
        }
    }

    public void zoneClickListener(View view) {
        if (view.getId() == R.id.combatZone_1_5) {
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    assert doc != null;
                    zoneDepth = (List) doc.get("zoneDepth");

                    zone = new Zone(1, 5,
                            (long)Objects.requireNonNull(zoneDepth).get(0));

                    findViewById(R.id.zoneScrollView).setVisibility(View.GONE);
                    currentLayout = findViewById(R.id.mobListLayout);
                    currentLayout.setVisibility(View.VISIBLE);

                    updateZoneMobList();
                } else
                    Log.d(TAG, "DocRef get failed with ", task.getException());
            });
        } else if (view.getId() == R.id.combatZone_5_10) {
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    assert doc != null;
                    zoneDepth = (List) doc.get("zoneDepth");

                    zone = new Zone(5, 10,
                            (long)Objects.requireNonNull(zoneDepth).get(1));

                    findViewById(R.id.zoneScrollView).setVisibility(View.GONE);
                    currentLayout = findViewById(R.id.mobListLayout);
                    currentLayout.setVisibility(View.VISIBLE);

                    updateZoneMobList();
                } else
                    Log.d(TAG, "DocRef get failed with ", task.getException());
            });
        } else if (view.getId() == R.id.combatZone_10_15) {
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    assert doc != null;
                    zoneDepth = (List) doc.get("zoneDepth");

                    zone = new Zone(10, 15,
                            (long)Objects.requireNonNull(zoneDepth).get(2));

                    findViewById(R.id.zoneScrollView).setVisibility(View.GONE);
                    currentLayout = findViewById(R.id.mobListLayout);
                    currentLayout.setVisibility(View.VISIBLE);

                    updateZoneMobList();
                } else
                    Log.d(TAG, "DocRef get failed with ", task.getException());
            });
        } else if (view.getId() == R.id.combatZone_15_20) {
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    assert doc != null;
                    zoneDepth = (List) doc.get("zoneDepth");

                    zone = new Zone(15, 20,
                            (long)Objects.requireNonNull(zoneDepth).get(3));

                    findViewById(R.id.zoneScrollView).setVisibility(View.GONE);
                    currentLayout = findViewById(R.id.mobListLayout);
                    currentLayout.setVisibility(View.VISIBLE);

                    updateZoneMobList();
                } else
                    Log.d(TAG, "DocRef get failed with ", task.getException());
            });
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> CombatActivity.super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
    }
}
