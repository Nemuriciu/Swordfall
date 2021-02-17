package com.Nemuriciu.Swordfall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CombatActivity extends AppCompatActivity {

    private static final String TAG = "CombatActivity";
    private String uid;
    private String username;
    private long level, gold, currentStam, maxStam, currentExp, maxExp;
    private Stats stats;

    private TextView usernameText, levelText, goldText, staminaText;
    private ImageView avatar;
    private ProgressBar expBar;

    private FirebaseFirestore db;
    private Resources res;
    //private List<Object> zoneDepth;
    private Zone zone;
    private ArrayList<Creature> zoneCreatures;
    private CombatInfo combat;
    private Item droppedItem;
    private JSONObject items;

    private ArrayList<String> bagSlots;
    private ArrayList<String> eqSlots;

    private boolean zoneLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combat);
        db = FirebaseFirestore.getInstance();
        res = getResources();

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        getExtras(extras);

        setViews();
        updateUI();
        setMenuButtons();
        //getDepthFromDB();

        try {
            items = new JSONObject(Utility.loadJSONFromAsset("items.json", this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStamina();
    }

    private void setViews() {
        avatar = findViewById(R.id.avatar);
        usernameText = findViewById(R.id.username);
        levelText = findViewById(R.id.level);
        goldText = findViewById(R.id.infoGold);
        staminaText = findViewById(R.id.infoStamina);
        expBar = findViewById(R.id.expBar);
        Drawable progressDrawable = new ProgressDrawable(
                Color.parseColor("#EA7500"), Color.parseColor("#442200"));
        expBar.setProgressDrawable(progressDrawable);
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

        stats = (Stats) extras.getSerializable("stats");

        bagSlots = extras.getStringArrayList("bagSlots");
        eqSlots = extras.getStringArrayList("eqSlots");
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        avatar.setImageResource(R.drawable.app_icon);
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
                intent.putExtra("stats", stats);
                intent.putStringArrayListExtra("bagSlots", bagSlots);
                intent.putStringArrayListExtra("eqSlots", eqSlots);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void getCreatures(String zoneName, int level) throws JSONException {
        zoneCreatures = new ArrayList<>();

        // Parse Creatures JSON //
        JSONObject obj = new JSONObject(Utility.loadJSONFromAsset("creatures.json", this));
        JSONArray mobList = obj.getJSONArray(zoneName);

        for (int i = 0; i < mobList.length(); i++) {
            JSONObject mob = mobList.getJSONObject(i);
            //JSONArray mobDepth = mob.getJSONArray("level");
            //boolean valid = false;

            JSONArray hp = mob.getJSONArray("hp");
            JSONArray atk = mob.getJSONArray("atk");
            JSONArray def = mob.getJSONArray("def");
            JSONArray dmg = mob.getJSONArray("dmg");
            JSONArray luck = mob.getJSONArray("luck");
            JSONArray exp = mob.getJSONArray("exp");
            JSONArray gold = mob.getJSONArray("gold");
            //JSONArray items = mob.getJSONArray("items");
            //JSONArray drops = mob.getJSONArray("drops");

            zoneCreatures.add(new Creature(mob.getInt("id"), mob.getInt("level"),
                    mob.getString("name"), mob.getString("avatar"), hp.getInt(0),
                    hp.getInt(1), atk.getInt(0), atk.getInt(1), def.getInt(0),
                    def.getInt(1), dmg.getInt(0), dmg.getInt(1), luck.getInt(0),
                    luck.getInt(1), exp.getInt(0), exp.getInt(1), gold.getInt(0),
                    gold.getInt(1)));
        }
    }

    private void generateCreature() {
        Random r = new Random();
        Creature c = zoneCreatures.get(r.nextInt(zoneCreatures.size()));
        combat = new CombatInfo(this);
        combat.setEnemy(c);
        startCombat();
    }

    @SuppressLint("SetTextI18n")
    private void updateExpGold(int combatExp, int combatGold) {
        boolean lvlUp = false;
        // Check level up //
        if (currentExp + combatExp >= maxExp) {
            levelText.setText(String.format(Locale.ENGLISH,"%,d", ++level));

            currentExp = currentExp + combatExp - maxExp;
            maxExp = (long) Utility.getNextLevelExp(level + 1);

            expBar.setMax((int) maxExp);
            expBar.setProgress((int)currentExp);

            stats.unspentPts += 5;
            findViewById(R.id.lvlUpIcon).setVisibility(View.VISIBLE);

            lvlUp = true;
        }
        else {
            currentExp += combatExp;
            expBar.setProgress((int)currentExp);
        }

        // Update Gold //
        gold += combatGold;
        goldText.setText(String.format(Locale.ENGLISH,"%,d", gold));

        // Add New Gold & New Exp to db //
        Map<String, Object> data = new HashMap<>();
        data.put("currentExp", currentExp);
        data.put("gold", gold);

        if (lvlUp) {
            data.put("level", level);
            data.put("maxExp", maxExp);
            data.put("stats", stats);
        }

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
                //Log.e(TAG, "Timer: " + mins + ":" + secs);

                // Get 10 Stamina / 15 min //
                long ticks = mins / 15;

                if (ticks > 0) {
                    currentStam = (currentStam + ticks * 10 > maxStam) ?
                            maxStam : currentStam + ticks * 10;
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

    private boolean addItemToBag(String id) {
        if (Collections.frequency(bagSlots, "0") == 0)
            return false;

        for (int i = 0; i < bagSlots.size(); i++) {
            if (bagSlots.get(i).equals("0")) {
                bagSlots.set(i, id);

                db.collection("users").document(uid)
                        .update("bagSlots", bagSlots)
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));

                return true;
            }
        }

        return false;
    }

    @SuppressLint("SetTextI18n")
    public void startCombat() {
        // Get CombatInfo Page Views //
        TextView playerName = findViewById(R.id.combatNamePlayer);
        TextView enemyName = findViewById(R.id.combatNameEnemy);
        ImageView playerAvatar = findViewById(R.id.combatAvatarPlayer);
        ImageView enemyAvatar = findViewById(R.id.combatAvatarEnemy);
        TextView playerHealthText = findViewById(R.id.combatPlayerHpText);
        TextView enemyHealthText = findViewById(R.id.combatEnemyHpText);
        ProgressBar playerHealth = findViewById(R.id.combatPlayerHp);
        ProgressBar enemyHealth = findViewById(R.id.combatEnemyHp);

        combat.playerName = username;
        combat.playerHealth = playerHealth;
        combat.playerHealthText = playerHealthText;
        combat.enemyHealth = enemyHealth;
        combat.enemyHealthText = enemyHealthText;
        combat.playerHitText = findViewById(R.id.combatHitPlayer);
        combat.enemyHitText = findViewById(R.id.combatHitEnemy);
        combat.log = findViewById(R.id.combatLog);
        combat.scroll = findViewById(R.id.combatLogScroll);

        int playerHp = (int)Math.round(stats.vit * 6.12);

        // Set Names //
        playerName.setText(username);
        enemyName.setText(combat.enemyName);

        // Set Avatars //
        playerAvatar.setImageDrawable(avatar.getDrawable());
        enemyAvatar.setImageResource(res.getIdentifier(
                combat.enemyAvatar, "drawable", getPackageName()));

        // Set Health Bars //
        enemyHealth.setMax(combat.enemyHp);
        enemyHealth.setProgress(enemyHealth.getMax());
        enemyHealthText.setText(enemyHealth.getProgress() + "/" + enemyHealth.getMax());

        playerHealth.setMax(playerHp);
        playerHealth.setProgress(playerHealth.getMax());
        playerHealthText.setText(playerHealth.getProgress() + "/" + playerHealth.getMax());

        combat.log.removeAllViews();
        combat.playerTurn = true;
        findViewById(R.id.mobCombatLayout).setVisibility(View.VISIBLE);
    }

    private void playerAttack() {

        // TODO: Dmg Formula (ATK / DEF)
        ThreadLocalRandom r = ThreadLocalRandom.current();
        long dmg = r.nextLong(stats.dmgMin, stats.dmgMax);

        combat.dealDamage("Enemy", dmg);
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

        switch (view.getId()) {
            case R.id.combatZone_1:
                // Manage stamina //
                /*
                int stamReq = 1;
                if (currentStam >= stamReq)
                    consumeStamina(stamReq);
                else {
                    zoneLock = false;
                    Toast.makeText(this, "Not enough stamina to enter zone.", Toast.LENGTH_SHORT).show();
                    return;
                }
                */

                zone = new Zone(0, "Swamp", (int)level);

                try {
                    getCreatures(zone.name.toLowerCase(), zone.level);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                generateCreature();
                break;

            case R.id.combatZone_2:
                // TODO:
                break;
        }
    }

    public void combatClickListener(View view) {
        if (!combat.playerTurn) return;
        combat.playerTurn = false;

        switch (view.getId()) {
            case R.id.combatButtonAtk:
                playerAttack();
                break;

            case R.id.combatButtonRetreat:
                // TODO:
                findViewById(R.id.mobCombatLayout).setVisibility(View.GONE);
                findViewById(R.id.zoneScrollView).setVisibility(View.VISIBLE);
                zoneLock = false;
                combat.playerTurn = true;
                break;
        }

    }

    @SuppressLint("SetTextI18n")
    public void openTooltipListener(View view) {
        // Set Avatar //
        ((ImageView) findViewById(R.id.combatItemAvatar)).
                setImageResource(res.getIdentifier(
                        droppedItem.avatar, "drawable", getPackageName()));

        // Set Name //
        TextView name = findViewById(R.id.combatItemName);
        name.setText(droppedItem.name);

        // Set Name Color based on Rarity //
        int color = Color.WHITE;
        switch (droppedItem.rarity) {
            case "common":
                color = Color.parseColor("#808080");
                break;
            case "uncommon":
                color = Color.parseColor("#4CAF50");
                break;
            case "rare":
                color = Color.parseColor("#3F51B5");
                break;
            case "epic":
                color = Color.parseColor("#673AB7");
                break;
            case "legendary":
                color = Color.parseColor("#FF9800");
                break;
        }
        name.setTextColor(color);

        // Set Level Requirement //
        TextView levelReq = findViewById(R.id.combatItemLevel);
        levelReq.setText("Level " + droppedItem.lvlReq);

        // Set Color to Red if Req not met //
        if (droppedItem.lvlReq > level)
            levelReq.setTextColor(Color.parseColor("#C80000"));
        else
            levelReq.setTextColor(Color.parseColor("#B4B4B4"));

        // Set Item Type //
        ((TextView) findViewById(R.id.combatItemType)).setText(
                droppedItem.type.substring(0, 1).toUpperCase() + droppedItem.type.substring(1));

        // Set Item Stats //
        if (droppedItem.atk > 0) {
            findViewById(R.id.mobItemAtkLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.mobItemAtk)).setText("+" + droppedItem.atk);
        } else
            findViewById(R.id.mobItemAtkLayout).setVisibility(View.GONE);

        if (droppedItem.def > 0) {
            findViewById(R.id.mobItemDefLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.mobItemDef)).setText("+" + droppedItem.def);
        } else
            findViewById(R.id.mobItemDefLayout).setVisibility(View.GONE);

        if (droppedItem.vit > 0) {
            findViewById(R.id.mobItemVitLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.mobItemVit)).setText("+" + droppedItem.vit);
        } else
            findViewById(R.id.mobItemVitLayout).setVisibility(View.GONE);

        if (droppedItem.dmgMin > 0 && droppedItem.dmgMax > 0) {
            findViewById(R.id.mobItemDmgLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.mobItemDmg)).setText(
                    droppedItem.dmgMin + "-" + droppedItem.dmgMax);
        } else
            findViewById(R.id.mobItemDmgLayout).setVisibility(View.GONE);

        if (droppedItem.luck > 0) {
            findViewById(R.id.mobItemLuckLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.mobItemLuck)).setText("+" + droppedItem.luck);
        } else
            findViewById(R.id.mobItemLuckLayout).setVisibility(View.GONE);

        findViewById(R.id.combatItemTooltipLayout).setVisibility(View.VISIBLE);
    }

    public void closeTooltipListener(View view) {
        if (view.getId() == R.id.combatItemTooltipLayout ||
                view.getId() == R.id.combatItemClose) {
            findViewById(R.id.combatItemTooltipLayout).setVisibility(View.GONE);
        }
    }

    public void exitZoneClickListener(View view) {
        if (view.getId() == R.id.mobListClose) {
            if (findViewById(R.id.mobCombatLayout).getVisibility() == View.VISIBLE) return;
            zoneLock = false;
            findViewById(R.id.mobListLayout).setVisibility(View.GONE);
            findViewById(R.id.zoneScrollView).setVisibility(View.VISIBLE);
        }
    }
}
