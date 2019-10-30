package com.Nemuriciu.Swordfall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class CombatActivity extends AppCompatActivity {

    private static final String TAG = "CombatActivity";
    private String uid;
    private String username;
    private long level, gold, currentStam, maxStam, currentExp, maxExp;
    private long atk, def, vit, dmg, luck;

    private TextView usernameText, levelText, goldText, staminaText;
    private ProgressBar expBar;

    private FirebaseFirestore db;
    private List<Object> zoneDepth;
    private Zone zone;
    private ArrayList<Creature> zoneCreatures;

    private ArrayList<String> bagSlots;
    private ArrayList<String> eqSlots;

    protected static boolean attackLock;
    private boolean zoneLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combat);
        db = FirebaseFirestore.getInstance();

        usernameText = findViewById(R.id.username);
        levelText = findViewById(R.id.level);
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
        getDepthFromDB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStamina();
    }

    private void getExtras(Bundle extras) {
        uid = extras.getString("uid");
        username = extras.getString("username");
        level = extras.getLong("level");
        gold = extras.getLong("gold");
        currentStam = extras.getLong("currentStamina");
        maxStam = extras.getLong("maxStamina");
        currentExp = extras.getLong("currentExp");
        maxExp = extras.getLong("maxExp");

        atk = extras.getLong("atk");
        def = extras.getLong("def");
        vit = extras.getLong("vit");
        dmg = extras.getLong("dmg");
        luck = extras.getLong("luck");

        bagSlots = extras.getStringArrayList("bagSlots");
        eqSlots = extras.getStringArrayList("eqSlots");
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        usernameText.setText(username);
        levelText.setText(String.format(Locale.ENGLISH,"%,d", level));
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
                intent.putExtra("level", level);
                intent.putExtra("gold", gold);
                intent.putExtra("currentStamina", currentStam);
                intent.putExtra("maxStamina", maxStam);
                intent.putExtra("currentExp", currentExp);
                intent.putExtra("maxExp", maxExp);
                intent.putExtra("atk", atk);
                intent.putExtra("def", def);
                intent.putExtra("vit", vit);
                intent.putExtra("dmg", dmg);
                intent.putExtra("luck", luck);
                intent.putStringArrayListExtra("bagSlots", bagSlots);
                intent.putStringArrayListExtra("eqSlots", eqSlots);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void getDepthFromDB() {
        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                assert doc != null;
                zoneDepth = Arrays.asList(
                        ((List) Objects.requireNonNull(doc.get("zoneDepth"))).toArray());
            } else
                Log.d(TAG, "DocRef get failed with ", task.getException());
        });
    }

    private void getCreatures(String zoneName, int depth) throws JSONException {
        zoneCreatures = new ArrayList<>();

        // Parse Creatures JSON //
        JSONObject obj = new JSONObject(Utility.loadJSONFromAsset("creatures.json", this));
        JSONArray mobList = obj.getJSONArray(zoneName);

        for (int i = 0; i < mobList.length(); i++) {
            JSONObject mob = mobList.getJSONObject(i);
            JSONArray mobDepth = mob.getJSONArray("depth");
            boolean valid = false;

            for (int j = 0; j < mobDepth.length(); j++) {
                if (mobDepth.getInt(j) == depth) {
                    valid = true;
                    break;
                }
            }

            if (valid) {
                JSONArray hp = mob.getJSONArray("hp");
                JSONArray atk = mob.getJSONArray("atk");
                JSONArray def = mob.getJSONArray("def");
                JSONArray dmg = mob.getJSONArray("dmg");
                JSONArray luck = mob.getJSONArray("luck");
                JSONArray exp = mob.getJSONArray("exp");
                JSONArray gold = mob.getJSONArray("gold");

                zoneCreatures.add(new Creature(mob.getInt("id"), mob.getInt("level"),
                        mob.getString("name"), mob.getString("avatar"), hp.getInt(0),
                        hp.getInt(1), atk.getInt(0), atk.getInt(1), def.getInt(0), def.getInt(1),
                        dmg.getInt(0), dmg.getInt(1), luck.getInt(0), luck.getInt(1),
                        exp.getInt(0), exp.getInt(1), gold.getInt(0), gold.getInt(1)));
            }
        }
    }

    private void updateZoneMobList() {                  // TODO: No mobList reset on same session
        TextView depthText = findViewById(R.id.zoneDepthLevel);
        LinearLayout mobList = findViewById(R.id.mobScrollList);
        mobList.removeAllViews();
        depthText.setText(String.valueOf(zone.depth));

        Random r = new Random();
        for (int i = 0; i < r.nextInt(5) + 6; i++) {
            Creature c = zoneCreatures.get(r.nextInt(zoneCreatures.size()));
            MobCard mobCard = new MobCard(this, c);

            mobList.addView(mobCard);
        }
    }

    private void updateExpGold(int combatExp, int combatGold) {
        // Check level up //
        if (currentExp + combatExp >= maxExp) {
            currentExp = currentExp + combatExp - maxExp;
            expBar.setProgress((int)currentExp);
            levelText.setText(String.format(Locale.ENGLISH,"%,d", ++level));

            // Add Level Up to db //
            db.collection("users").document(uid)
                    .update("level", level)
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));
        } else {
            currentExp += combatExp;
            expBar.setProgress((int)currentExp);
        }

        // Update Gold //
        gold += combatGold;
        goldText.setText(String.format(Locale.ENGLISH,"%,d", gold));

        // Add New Gold & New Exp to db //
        Map<String, Object> data = new HashMap<>();
        data.put("currentExp", currentExp);
        data.put("maxExp", maxExp);
        data.put("gold", gold);

        db.collection("users").document(uid)
                .update(data)
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));
    }

    @SuppressLint("SetTextI18n")
    private void updateStamina() {
        if (currentStam >= maxStam) return;

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                assert doc != null;
                Timestamp serverTime = doc.getTimestamp("staminaTime");
                Timestamp currentTime = Timestamp.now();

                assert serverTime != null;
                long diff = currentTime.toDate().getTime() - serverTime.toDate().getTime();
                long mins = diff / 1000 / 60;
                long secs = diff / 1000 % 60;
                Log.e(TAG, "Timer: " + mins + ":" + secs);

                // Get 1 tick of stamina every 5 min //
                long ticks = mins / 5;

                if (ticks > 0) {
                    currentStam = (currentStam + ticks * 15 > maxStam) ?
                            maxStam : currentStam + ticks * 15;
                    staminaText.setText(String.format(Locale.ENGLISH,"%,d", currentStam) + "/"
                            + String.format(Locale.ENGLISH,"%,d", maxStam));

                    db.collection("users").document(uid)
                            .update("currentStamina", currentStam)
                            .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));

                    if (currentStam < maxStam) {
                        long tickDiff = mins % 5;
                        Date newStamp = new Date(System.currentTimeMillis() -
                                (tickDiff * 60000 + secs * 1000));

                        db.collection("users").document(uid)
                                .update("staminaTime", newStamp)
                                .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));
                    }
                }
            } else
                Log.d(TAG, "DocRef get failed with ", task.getException());
        });
    }

    @SuppressLint("SetTextI18n")
    private void consumeStamina(int value) {
        if (currentStam >= maxStam && (currentStam - value) < maxStam) {
            Timestamp staminaTime = Timestamp.now();
            db.collection("users").document(uid)
                    .update("staminaTime", staminaTime)
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));
        }

        currentStam -= value;
        staminaText.setText(String.format(Locale.ENGLISH,"%,d", currentStam) + "/"
                + String.format(Locale.ENGLISH,"%,d", maxStam));

        db.collection("users").document(uid)
                .update("currentStamina", currentStam)
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));
    }

    @SuppressLint("SetTextI18n")
    public void startCombat(MobCard mobCard) {
        // Manage stamina //
        int stamReq = 1;
        if (currentStam >= stamReq)
            consumeStamina(stamReq);
        else {
            attackLock = false;
            Toast.makeText(this, "Not enough stamina.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get Combat Page Views //
        ImageView playerAvatar = findViewById(R.id.mobCombatPlayerAvatar);
        ImageView mobAvatar = findViewById(R.id.mobCombatMobAvatar);
        TextView mobHpBarText = findViewById(R.id.mobCombatMobHpText);
        TextView playerHpBarText = findViewById(R.id.mobCombatPlayerHpText);
        ProgressBar mobHpBar = findViewById(R.id.mobCombatMobHp);
        ProgressBar playerHpBar = findViewById(R.id.mobCombatPlayerHp);
        TextView combatExpText = findViewById(R.id.mobCombatExp);
        TextView combatGoldText = findViewById(R.id.mobCombatGold);

        int playerHp = (int)Math.round(vit * 6.12);

        // Set Player Avatar //
        Drawable d = findViewById(R.id.avatar).getBackground();
        playerAvatar.setImageDrawable(d);

        // Set Mob Avatar //
        mobAvatar.setImageResource(getResources().getIdentifier(
                mobCard.mob.avatar, "drawable", getPackageName()));

        // Calculate Combat Result //
        CombatManager combatManager = new CombatManager(username, mobCard.mob.name,
                level, mobCard.mob.level, playerHp, mobCard.getHp(), atk, mobCard.getAtk(),
                def, mobCard.getDef(), dmg, mobCard.getDmg(), luck, mobCard.getLuck());

        String result = combatManager.simulateCombat();
        TextView resultText = findViewById(R.id.mobCombatResult);
        ArrayList<String> log = combatManager.getCombatLog();

        // Set Result Title and Color //
        int red = Color.parseColor("#AA0000");
        int green = Color.parseColor("#00AA00");
        resultText.setText(result);
        switch (result) {
            case "Victory":
                resultText.setTextColor(green);
                break;
            case "Defeat":
                resultText.setTextColor(red);
                break;
            case "Inconclusive":
                resultText.setTextColor(Color.parseColor("#C89600"));
                break;
        }

        // Set Mob Health //
        mobHpBar.setMax(mobCard.getHp());
        mobHpBar.setProgress((int)combatManager.getEnemyHp());
        mobHpBarText.setText(mobHpBar.getProgress() + "/" + mobHpBar.getMax());

        // Set Player Health //
        playerHpBar.setMax(playerHp);
        playerHpBar.setProgress((int)combatManager.getPlayerHp());
        playerHpBarText.setText(playerHpBar.getProgress() + "/" + playerHpBar.getMax());

        // Fill Combat Log Scrollview //
        LinearLayout logList = findViewById(R.id.mobCombatLog);
        logList.removeAllViews();

        for (int i = 0; i < log.size(); i++) {
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(4,6,4,0);
            tv.setLayoutParams(params);
            tv.setText(log.get(i));
            tv.setTextSize(10);
            tv.setGravity(Gravity.CENTER);

            if (i % 2 != 0)
                tv.setTextColor(red);

            logList.addView(tv);
        }

        // Set Reward if Victory //
        if (result.equals("Victory")) {
            int combatExp = mobCard.getExp();
            int combatGold = mobCard.getGold();
            combatExpText.setText(String.format(Locale.ENGLISH,"%,d", combatExp) + "  Exp");
            combatGoldText.setText(String.format(Locale.ENGLISH,"%,d", combatGold) + "  Gold");
            updateExpGold(combatExp, combatGold);

            // Remove MobCard from list //
            LinearLayout mobList = findViewById(R.id.mobScrollList);
            mobList.removeView(mobCard);

            // Refresh list if empty //
            if (mobList.getChildCount() == 0)
                updateZoneMobList();
        } else {
            combatExpText.setText("0  Exp");
            combatGoldText.setText("0  Gold");
        }

        findViewById(R.id.mobCombatLayout).setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> CombatActivity.super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * CombatActivity Click Listeners
     */
    public void zoneClickListener(View view) {
        if (zoneLock) return;
        zoneLock = true;
        attackLock = false;

        switch (view.getId()) {
            case R.id.combatZone_1:
                zone = new Zone(0, "Swamp", (long)zoneDepth.get(0));

                try {
                    getCreatures(zone.name.toLowerCase(), (int)zone.depth);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                updateZoneMobList();
                findViewById(R.id.mobListLayout).setVisibility(View.VISIBLE);
                break;

            case R.id.combatZone_2:
                break;
        }
    }

    public void mobListClickListener(View view) {
        switch (view.getId()) {
            case R.id.mobListAdvanceButton:
                if (zone.depth < 20) {
                    // Manage stamina //
                    int stamReq = 1;
                    if (currentStam >= stamReq)
                        consumeStamina(stamReq);
                    else {
                        Toast.makeText(this, "Not enough stamina.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    zone.depth++;
                    zoneDepth.set(zone.index, zone.depth);
                    ((TextView)findViewById(R.id.zoneDepthLevel)).
                            setText(String.valueOf(zone.depth));

                    db.collection("users").document(uid)
                            .update("zoneDepth", zoneDepth)
                            .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));

                    try {
                        getCreatures(zone.name.toLowerCase(), (int)zone.depth);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    updateZoneMobList();
                } else
                    Toast.makeText(this, "Maximum Depth Reached", Toast.LENGTH_SHORT).show();
                break;

            case R.id.mobListRefreshButton:
                // Manage stamina //
                int stamReq = 1;
                if (currentStam >= stamReq)
                    consumeStamina(stamReq);
                else {
                    Toast.makeText(this, "Not enough stamina.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    getCreatures(zone.name.toLowerCase(), (int)zone.depth);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                updateZoneMobList();
                break;
        }
    }

    public void exitZoneClickListener(View view) {
        if (view.getId() == R.id.mobListClose) {
            if (findViewById(R.id.mobCombatLayout).getVisibility() == View.VISIBLE) return;
            zoneLock = false;
            findViewById(R.id.mobListLayout).setVisibility(View.GONE);
            findViewById(R.id.zoneScrollView).setVisibility(View.VISIBLE);
        } else if (view.getId() == R.id.mobCombatClose) {
            attackLock = false;
            findViewById(R.id.mobCombatLayout).setVisibility(View.GONE);
        }
    }
}
