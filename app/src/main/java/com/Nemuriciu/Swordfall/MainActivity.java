package com.Nemuriciu.Swordfall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements View.OnDragListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    private Resources r;

    private String uid;
    private String username;
    private long level, gold, currentStam, maxStam, currentExp, maxExp;
    private Stats stats;
    private Stats tempStats;

    private TextView usernameText, levelText, goldText, staminaText;
    private TextView atkText, defText, vitText, dmgText, luckText;
    private ImageView avatar;
    private ProgressBar expBar;
    private FirebaseFirestore db;
    private JSONObject items;

    private ImageView helm, gloves, chest, shoulder, weapon, pants, offhand, boots;
    private ArrayList<ImageView> bagImages;
    private ArrayList<String> bagSlots;
    private ArrayList<String> eqSlots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        r = getResources();

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        getExtras(extras);

        setViews();
        updateUI();
        setMenuButtons();
        addListeners();

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

        atkText = findViewById(R.id.statsAtkText);
        defText = findViewById(R.id.statsDefText);
        vitText = findViewById(R.id.statsVitText);
        dmgText = findViewById(R.id.statsDmgText);
        luckText = findViewById(R.id.statsLuckText);

        helm = findViewById(R.id.eq_helm);
        gloves = findViewById(R.id.eq_gloves);
        chest = findViewById(R.id.eq_chest);
        shoulder = findViewById(R.id.eq_shoulder);
        weapon = findViewById(R.id.eq_weapon);
        pants = findViewById(R.id.eq_pants);
        offhand = findViewById(R.id.eq_offhand);
        boots = findViewById(R.id.eq_boots);

        bagImages = new ArrayList<>();
        for (int i = 0; i < 56; i++) {
            bagImages.add(findViewById(r.getIdentifier("bagSlot_" + i, "id", getPackageName())));
        }
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
        levelText.setText(String.format(Locale.ENGLISH, "%,d", level));
        goldText.setText(String.format(Locale.ENGLISH, "%,d", gold));
        staminaText.setText(String.format(Locale.ENGLISH, "%,d", currentStam) + "/"
                + String.format(Locale.ENGLISH, "%,d", maxStam));
        expBar.setMax((int) maxExp);
        expBar.setProgress((int) currentExp);

        atkText.setText(String.format(Locale.ENGLISH, "%,d", stats.atk));
        defText.setText(String.format(Locale.ENGLISH, "%,d", stats.def));
        vitText.setText(String.format(Locale.ENGLISH, "%,d", stats.vit));
        dmgText.setText(String.format(Locale.ENGLISH, "%,d", stats.dmgMin)
                + "-"
                + String.format(Locale.ENGLISH, "%,d", stats.dmgMax));
        luckText.setText(String.format(Locale.ENGLISH, "%,d", stats.luck));

        if  (stats.unspentPts > 0) {
            ((TextView)findViewById(R.id.statsPoints)).setText(
                    String.valueOf(stats.unspentPts));
            findViewById(R.id.statsNrLayout).setVisibility(View.VISIBLE);

            findViewById(R.id.statsAtkPlus).setVisibility(View.VISIBLE);
            findViewById(R.id.statsDefPlus).setVisibility(View.VISIBLE);
            findViewById(R.id.statsVitPlus).setVisibility(View.VISIBLE);
            findViewById(R.id.statsLuckPlus).setVisibility(View.VISIBLE);

            try {
                tempStats = (Stats)stats.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }


        String id = eqSlots.get(0);
        if (!id.equals("0")) {
            helm.setImageResource(r.getIdentifier(
                    "item_" + id, "drawable", getPackageName()));
            helm.setTag("item_" + id);
        }

        id = eqSlots.get(1);
        if (!id.equals("0")) {
            gloves.setImageResource(r.getIdentifier(
                    "item_" + id, "drawable", getPackageName()));
            gloves.setTag("item_" + id);
        }

        id = eqSlots.get(2);
        if (!id.equals("0")) {
            chest.setImageResource(r.getIdentifier(
                    "item_" + id, "drawable", getPackageName()));
            chest.setTag("item_" + id);
        }

        id = eqSlots.get(3);
        if (!id.equals("0")) {
            shoulder.setImageResource(r.getIdentifier(
                    "item_" + id, "drawable", getPackageName()));
            shoulder.setTag("item_" + id);
        }

        id = eqSlots.get(4);
        if (!id.equals("0")) {
            weapon.setImageResource(r.getIdentifier(
                    "item_" + eqSlots.get(4), "drawable", getPackageName()));
            weapon.setTag("item_" + id);
        }

        id = eqSlots.get(5);
        if (!id.equals("0")) {
            pants.setImageResource(r.getIdentifier(
                    "item_" + eqSlots.get(5), "drawable", getPackageName()));
            pants.setTag("item_" + id);
        }

        id = eqSlots.get(6);
        if (!id.equals("0")) {
            offhand.setImageResource(r.getIdentifier(
                    "item_" + eqSlots.get(6), "drawable", getPackageName()));
            offhand.setTag("item_" + id);
        }

        id = eqSlots.get(7);
        if (!id.equals("0")) {
            boots.setImageResource(r.getIdentifier(
                    "item_" + eqSlots.get(7), "drawable", getPackageName()));
            boots.setTag("item_" + id);
        }

        for (int i = 0; i < 56; i++) {
            ImageView view = bagImages.get(i);
            id = bagSlots.get(i);

            if (!id.equals("0")) {
                view.setImageResource(r.getIdentifier(
                        "item_" + id, "drawable", getPackageName()));
                view.setTag("item_" + id);
            }
        }
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
    private void updateStats(Item newItem, Item removedItem) {
        // Decrease stats upon removing item //
        if (removedItem != null) {
            stats.atk -= removedItem.atk;
            stats.def -= removedItem.def;
            stats.vit -= removedItem.vit;
            stats.dmgMin -= removedItem.dmgMin;
            stats.dmgMax -= removedItem.dmgMax;
            stats.luck-= removedItem.luck;
        }

        // Add stats upon adding item //
        if (newItem != null) {
            stats.atk += newItem.atk;
            stats.def += newItem.def;
            stats.vit += newItem.vit;
            stats.dmgMin += newItem.dmgMin;
            stats.dmgMax += newItem.dmgMax;
            stats.luck += newItem.luck;
        }

        atkText.setText(String.format(Locale.ENGLISH, "%,d", stats.atk));
        defText.setText(String.format(Locale.ENGLISH, "%,d", stats.def));
        vitText.setText(String.format(Locale.ENGLISH, "%,d", stats.vit));
        dmgText.setText(String.format(Locale.ENGLISH, "%,d", stats.dmgMin)
                + "-"
                + String.format(Locale.ENGLISH, "%,d", stats.dmgMax));
        luckText.setText(String.format(Locale.ENGLISH, "%,d", stats.luck));

        try {
            tempStats = (Stats) stats.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
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
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void addListeners() {
        findViewById(R.id.eq_helm).setOnLongClickListener(this);
        findViewById(R.id.eq_helm).setOnDragListener(this);
        findViewById(R.id.eq_helm).setOnClickListener(this::openTooltipListener);
        findViewById(R.id.eq_gloves).setOnLongClickListener(this);
        findViewById(R.id.eq_gloves).setOnDragListener(this);
        findViewById(R.id.eq_gloves).setOnClickListener(this::openTooltipListener);
        findViewById(R.id.eq_chest).setOnLongClickListener(this);
        findViewById(R.id.eq_chest).setOnDragListener(this);
        findViewById(R.id.eq_chest).setOnClickListener(this::openTooltipListener);
        findViewById(R.id.eq_shoulder).setOnLongClickListener(this);
        findViewById(R.id.eq_shoulder).setOnDragListener(this);
        findViewById(R.id.eq_shoulder).setOnClickListener(this::openTooltipListener);
        findViewById(R.id.eq_pants).setOnLongClickListener(this);
        findViewById(R.id.eq_pants).setOnDragListener(this);
        findViewById(R.id.eq_pants).setOnClickListener(this::openTooltipListener);
        findViewById(R.id.eq_boots).setOnLongClickListener(this);
        findViewById(R.id.eq_boots).setOnDragListener(this);
        findViewById(R.id.eq_boots).setOnClickListener(this::openTooltipListener);
        findViewById(R.id.eq_weapon).setOnLongClickListener(this);
        findViewById(R.id.eq_weapon).setOnDragListener(this);
        findViewById(R.id.eq_weapon).setOnClickListener(this::openTooltipListener);
        findViewById(R.id.eq_offhand).setOnLongClickListener(this);
        findViewById(R.id.eq_offhand).setOnDragListener(this);
        findViewById(R.id.eq_offhand).setOnClickListener(this::openTooltipListener);

        for (int i = 0; i < 56; i++) {
            String bagId = "bagSlot_" + i;
            int id = r.getIdentifier(bagId, "id", getPackageName());
            ImageView view = findViewById(id);
            view.setOnDragListener(this);
            view.setOnLongClickListener(this);
            view.setOnClickListener(this::openTooltipListener);
        }
    }

    private void swapItems(ImageView owner, ImageView target) {
        String ownerImg = owner.getTag().toString();
        String ownerId = r.getResourceEntryName(owner.getId());
        String targetImg = target.getTag().toString();
        String targetId = r.getResourceEntryName(target.getId());

        /*  Bag -> Bag  */
        if (ownerId.substring(0, 3).equals("bag") && targetId.substring(0, 3).equals("bag")) {

            int targetBagIx = Integer.valueOf(targetId.substring(8));
            int ownerBagIx = Integer.valueOf(ownerId.substring(8));

            target.setImageResource(r.getIdentifier(ownerImg, "drawable", getPackageName()));
            target.setTag(ownerImg);

            if (targetImg.equals("null")) {
                owner.setImageResource(r.getIdentifier(
                        "main_inventory_cell", "drawable", getPackageName()));
                owner.setTag("null");
                bagSlots.set(ownerBagIx, "0");
            } else {
                owner.setImageResource(r.getIdentifier(targetImg, "drawable", getPackageName()));
                owner.setTag(targetImg);
                bagSlots.set(ownerBagIx, targetImg.substring(5));
            }

            bagSlots.set(targetBagIx, ownerImg.substring(5));

            Map<String, Object> data = new HashMap<>();
            data.put("bagSlots", bagSlots);
            data.put("stats", stats);

            db.collection("users").document(uid)
                    .update(data)
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));
        }
        /* Equipment -> Bag */
        else if (targetId.substring(0, 3).equals("bag")) {

            Log.e(TAG, ownerImg);
            Item ownerItem = new Item(ownerImg.substring(5), items);
            Log.e(TAG, ownerItem.toString());

            int targetBagIx = Integer.valueOf(targetId.substring(8));
            int ownerEqIx = Utility.getEqArrayIndex(ownerItem.type);

            if (targetImg.equals("null")) {
                target.setImageResource(r.getIdentifier(ownerImg, "drawable", getPackageName()));
                target.setTag(ownerImg);
                owner.setImageResource(r.getIdentifier(ownerId, "drawable", getPackageName()));
                owner.setTag(ownerId.substring(3));

                // Update stats on equipment removal //
                updateStats(null, ownerItem);

                bagSlots.set(targetBagIx, ownerImg.substring(5));
                eqSlots.set(ownerEqIx, "0");
            } else {
                Item targetItem = new Item(targetImg.substring(5), items);

                // Check target item for valid equip //
                if (!targetItem.type.equals(ownerItem.type)) {
                    Toast.makeText(this, "Can't equip item in that slot.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (targetItem.lvlReq > level) {
                    Toast.makeText(this, "Level requirement not met.", Toast.LENGTH_SHORT).show();
                    return;
                }

                target.setImageResource(r.getIdentifier(ownerImg, "drawable", getPackageName()));
                target.setTag(ownerImg);
                owner.setImageResource(r.getIdentifier(targetImg, "drawable", getPackageName()));
                owner.setTag(targetImg);

                // Update stats on equipment exchange //
                updateStats(targetItem, ownerItem);

                bagSlots.set(targetBagIx, ownerImg.substring(5));
                eqSlots.set(ownerEqIx, targetImg.substring(5));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("bagSlots", bagSlots);
            data.put("eqSlots", eqSlots);
            data.put("stats", stats);

            db.collection("users").document(uid)
                    .update(data)
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));
        }
        /* Bag -> Equipment */
        else if (ownerId.substring(0, 3).equals("bag")) {

            Item ownerItem = new Item(ownerImg.substring(5), items);
            int targetEqIx = Utility.getEqArrayIndex(ownerItem.type);
            int ownerBagIx = Integer.valueOf(ownerId.substring(8));

            // Equip slot is empty //
            if (targetId.substring(0, 2).equals("eq")) {
                String targetType = targetId.substring(3);

                // Check target item for valid equip //
                if (!targetType.equals(ownerItem.type)) {
                    Toast.makeText(this, "Can't equip item in that slot.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (ownerItem.lvlReq > level) {
                    Toast.makeText(this, "Level requirement not met.", Toast.LENGTH_SHORT).show();
                    return;
                }

                target.setImageResource(r.getIdentifier(ownerImg, "drawable", getPackageName()));
                target.setTag(ownerImg);
                owner.setImageResource(r.getIdentifier(
                        "main_inventory_cell", "drawable", getPackageName()));
                owner.setTag("null");

                // Update stats on equipment add //
                updateStats(ownerItem, null);

                bagSlots.set(ownerBagIx, "0");
                eqSlots.set(targetEqIx, ownerImg.substring(5));
            } else {
                Item targetItem = new Item(targetImg.substring(5), items);

                // Check target item for valid equip //
                if (!ownerItem.type.equals(targetItem.type)) {
                    Toast.makeText(this, "Can't equip item in that slot.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (ownerItem.lvlReq > level) {
                    Toast.makeText(this, "Level requirement not met.", Toast.LENGTH_SHORT).show();
                    return;
                }

                target.setImageResource(r.getIdentifier(ownerImg, "drawable", getPackageName()));
                target.setTag(ownerImg);
                owner.setImageResource(r.getIdentifier(targetImg, "drawable", getPackageName()));
                owner.setTag(targetImg);

                // Update stats on equipment exchange //
                updateStats(ownerItem, targetItem);

                bagSlots.set(ownerBagIx, targetImg.substring(5));
                eqSlots.set(targetEqIx, ownerImg.substring(5));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("bagSlots", bagSlots);
            data.put("eqSlots", eqSlots);
            data.put("stats", stats);

            db.collection("users").document(uid)
                    .update(data)
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> MainActivity.super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Click Listeners
     */
    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
            case DragEvent.ACTION_DRAG_ENDED:
            case DragEvent.ACTION_DRAG_LOCATION:
            case DragEvent.ACTION_DRAG_ENTERED:
            case DragEvent.ACTION_DRAG_EXITED:
                return true;

            case DragEvent.ACTION_DROP:
                if (v.equals(event.getLocalState())) return false;

                swapItems((ImageView) event.getLocalState(), (ImageView) v);
                return true;
            default:
                throw new IllegalStateException("Unexpected value: " + event.getAction());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        String type = v.getTag().toString().substring(0, 4);
        if (!type.equals("item")) return false;

        ClipData clipData = ClipData.newPlainText("item", v.getTag().toString());
        View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(v);
        v.startDrag(clipData, dragShadowBuilder, v, 0);
        return true;
    }

    @SuppressLint("SetTextI18n")
    public void openTooltipListener(View view) {
        String viewTag = view.getTag().toString();
        String viewId = r.getResourceEntryName(view.getId());

        if (!viewTag.substring(0, 4).equals("item"))
            return;

        Item item = new Item(viewTag.substring(5), items);

        // Set Bag Slot ID //
        ((TextView)findViewById(R.id.itemBagSlot)).setText(viewId);

        // Set Avatar //
        ((ImageView) findViewById(R.id.itemTooltipAvatar)).
                setImageResource(r.getIdentifier(item.avatar, "drawable", getPackageName()));

        // Set Name //
        TextView name = findViewById(R.id.itemTooltipName);
        name.setText(item.name);

        // Set Name Color based on Rarity //
        int color = Color.WHITE;
        switch (item.rarity) {
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
        TextView levelReq = findViewById(R.id.itemTooltipLevel);
        levelReq.setText("Level " + item.lvlReq);

        // Set Color to Red if Req not met //
        if (item.lvlReq > level)
            levelReq.setTextColor(Color.parseColor("#C80000"));
        else
            levelReq.setTextColor(Color.parseColor("#B4B4B4"));

        // Set Item Type //
        ((TextView) findViewById(R.id.itemTooltipType)).setText(
                item.type.substring(0, 1).toUpperCase() + item.type.substring(1));

        // Set Item Stats //
        if (item.atk > 0) {
            findViewById(R.id.itemTooltipAtkLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.itemTooltipAtk)).setText("+" + item.atk);
        } else
            findViewById(R.id.itemTooltipAtkLayout).setVisibility(View.GONE);

        if (item.def > 0) {
            findViewById(R.id.itemTooltipDefLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.itemTooltipDef)).setText("+" + item.def);
        } else
            findViewById(R.id.itemTooltipDefLayout).setVisibility(View.GONE);

        if (item.vit > 0) {
            findViewById(R.id.itemTooltipVitLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.itemTooltipVit)).setText("+" + item.vit);
        } else
            findViewById(R.id.itemTooltipVitLayout).setVisibility(View.GONE);

        if (item.dmgMin > 0 && item.dmgMax > 0) {
            findViewById(R.id.itemTooltipDmgLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.itemTooltipDmg)).setText(item.dmgMin + "-" + item.dmgMax);
        } else
            findViewById(R.id.itemTooltipDmgLayout).setVisibility(View.GONE);

        if (item.luck > 0) {
            findViewById(R.id.itemTooltipLuckLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.itemTooltipLuck)).setText("+" + item.luck);
        } else
            findViewById(R.id.itemTooltipLuckLayout).setVisibility(View.GONE);

        // Show Sell Button if Bag Item //
        if (viewId.substring(0, 3).equals("bag")) {
            TextView sellPrice = findViewById(R.id.itemPriceValue);
            sellPrice.setText(String.valueOf(item.sellPrice));
            sellPrice.setVisibility(View.VISIBLE);

            findViewById(R.id.itemPriceIcon).setVisibility(View.VISIBLE);
            findViewById(R.id.itemTooltipSell).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.itemPriceValue).setVisibility(View.GONE);
            findViewById(R.id.itemPriceIcon).setVisibility(View.GONE);
            findViewById(R.id.itemTooltipSell).setVisibility(View.GONE);
        }


        findViewById(R.id.itemTooltipLayout).setVisibility(View.VISIBLE);
    }

    public void closeTooltipListener(View view) {
        int id = view.getId();
        if (id == R.id.itemTooltipLayout || id == R.id.itemTooltipClose)
            findViewById(R.id.itemTooltipLayout).setVisibility(View.GONE);
    }

    public void sellItemListener(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage("Confirm Sell?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog, which) -> {
            String slotId = (((TextView)findViewById(R.id.itemBagSlot)).getText().toString());
            ImageView bagSlot = findViewById(r.getIdentifier(slotId, "id", getPackageName()));

            gold += Long.valueOf(
                    ((TextView)findViewById(R.id.itemPriceValue)).getText().toString());
            goldText.setText(String.format(Locale.ENGLISH, "%,d", gold));

            bagSlot.setTag("null");
            bagSlot.setImageResource(r.getIdentifier(
                    "main_inventory_cell", "drawable", getPackageName()));
            bagSlots.set(Integer.valueOf(slotId.substring(8)), "0");

            findViewById(R.id.itemTooltipLayout).setVisibility(View.GONE);

            Map<String, Object> data = new HashMap<>();
            data.put("gold", gold);
            data.put("bagSlots", bagSlots);

            db.collection("users").document(uid)
                    .update(data)
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                (dialog, which) -> dialog.cancel());
        alertDialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setLayout(800, 400);

        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        TextView msg = alertDialog.findViewById(android.R.id.message);
        assert msg != null;
        msg.setTextSize(18);
        msg.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams btnParams =
                (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        btnParams.weight = 10;
        btnPositive.setLayoutParams(btnParams);
        btnNegative.setLayoutParams(btnParams);
    }

    public void statAllocListener(View view) {
        // Display Apply & Reset on first press //
        if (tempStats.unspentPts.equals(stats.unspentPts))
            findViewById(R.id.statsButtonsLayout).setVisibility(View.VISIBLE);

        switch (view.getId()) {
            case R.id.statsAtkPlus:
                tempStats.atk++;

                atkText.setText(String.format(Locale.ENGLISH, "%,d", tempStats.atk));
                break;

            case R.id.statsDefPlus:
                tempStats.def++;

                defText.setText(String.format(Locale.ENGLISH, "%,d", tempStats.def));
                break;

            case R.id.statsVitPlus:
                tempStats.vit++;

                vitText.setText(String.format(Locale.ENGLISH, "%,d", tempStats.vit));
                break;

            case R.id.statsLuckPlus:
                tempStats.luck++;

                luckText.setText(String.format(Locale.ENGLISH, "%,d", tempStats.luck));
                break;

            case R.id.statsApply:
                if (tempStats.unspentPts.equals(stats.unspentPts)) return;

                stats.atk = tempStats.atk;
                stats.def = tempStats.def;
                stats.vit = tempStats.vit;
                stats.luck = tempStats.luck;
                stats.unspentPts = tempStats.unspentPts;

                if (stats.unspentPts <= 0)
                    findViewById(R.id.statsNrLayout).setVisibility(View.GONE);

                findViewById(R.id.statsButtonsLayout).setVisibility(View.GONE);

                db.collection("users").document(uid)
                        .update("stats", stats)
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));

                return;

            case R.id.statsReset:
                if (tempStats.unspentPts.equals(stats.unspentPts)) return;

                atkText.setText(String.format(Locale.ENGLISH, "%,d", stats.atk));
                defText.setText(String.format(Locale.ENGLISH, "%,d", stats.def));
                vitText.setText(String.format(Locale.ENGLISH, "%,d", stats.vit));
                luckText.setText(String.format(Locale.ENGLISH, "%,d", stats.luck));

                if (tempStats.unspentPts <= 0) {
                    findViewById(R.id.statsAtkPlus).setVisibility(View.VISIBLE);
                    findViewById(R.id.statsDefPlus).setVisibility(View.VISIBLE);
                    findViewById(R.id.statsVitPlus).setVisibility(View.VISIBLE);
                    findViewById(R.id.statsLuckPlus).setVisibility(View.VISIBLE);
                }

                ((TextView)findViewById(R.id.statsPoints)).setText(
                        String.valueOf(stats.unspentPts));

                try {
                    tempStats = (Stats)stats.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

                return;
        }

        tempStats.unspentPts--;
        ((TextView)findViewById(R.id.statsPoints)).setText(
                String.valueOf(tempStats.unspentPts));

        if (tempStats.unspentPts <= 0) {
            findViewById(R.id.statsAtkPlus).setVisibility(View.GONE);
            findViewById(R.id.statsDefPlus).setVisibility(View.GONE);
            findViewById(R.id.statsVitPlus).setVisibility(View.GONE);
            findViewById(R.id.statsLuckPlus).setVisibility(View.GONE);
        }
    }
}
