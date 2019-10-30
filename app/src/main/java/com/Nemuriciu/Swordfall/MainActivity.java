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
import android.view.View;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity
        implements View.OnDragListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    private Resources r;
    private String uid;
    private String username;
    private long level, gold, currentStam, maxStam, currentExp, maxExp;
    private ArrayList<Long> stats;

    private TextView usernameText, levelText, goldText, staminaText;
    private TextView atkText, defText, vitText, dmgText, luckText;
    private ProgressBar expBar;
    private FirebaseFirestore db;
    private JSONObject items;

    private ImageView helm, gloves, chest, bracers, weapon, pants, offhand, boots;
    private ArrayList<ImageView> bagImages;
    private ArrayList<String> bagSlots;
    private ArrayList<String> eqSlots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        r = getResources();
        stats = new ArrayList<>();

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
        bracers = findViewById(R.id.eq_bracers);
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

        stats.add(extras.getLong("atk"));
        stats.add(extras.getLong("def"));
        stats.add(extras.getLong("vit"));
        stats.add(extras.getLong("dmg"));
        stats.add(extras.getLong("luck"));

        bagSlots = extras.getStringArrayList("bagSlots");
        eqSlots = extras.getStringArrayList("eqSlots");
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        usernameText.setText(username);
        levelText.setText(String.format(Locale.ENGLISH, "%,d", level));
        goldText.setText(String.format(Locale.ENGLISH, "%,d", gold));
        staminaText.setText(String.format(Locale.ENGLISH, "%,d", currentStam) + "/"
                + String.format(Locale.ENGLISH, "%,d", maxStam));
        expBar.setMax((int) maxExp);
        expBar.setProgress((int) currentExp);

        atkText.setText(String.format(Locale.ENGLISH, "%,d", stats.get(0)));
        defText.setText(String.format(Locale.ENGLISH, "%,d", stats.get(1)));
        vitText.setText(String.format(Locale.ENGLISH, "%,d", stats.get(2)));
        dmgText.setText(String.format(Locale.ENGLISH, "%,d", stats.get(3)));
        luckText.setText(String.format(Locale.ENGLISH, "%,d", stats.get(4)));

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
            bracers.setImageResource(r.getIdentifier(
                    "item_" + id, "drawable", getPackageName()));
            bracers.setTag("item_" + id);
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
                Log.e(TAG, "Timer: " + mins + ":" + secs);

                // Get 1 tick of stamina every 5 min //
                long ticks = mins / 5;

                if (ticks > 0) {
                    currentStam = (currentStam + ticks * 15 > maxStam) ?
                            maxStam : currentStam + ticks * 15;
                    staminaText.setText(String.format(Locale.ENGLISH, "%,d", currentStam) + "/"
                            + String.format(Locale.ENGLISH, "%,d", maxStam));

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

    private void updateStats(Item newItem, Item removedItem) {
        // Decrease stats upon removing item //
        if (removedItem != null) {
            stats.set(0, stats.get(0) - removedItem.atk);
            stats.set(1, stats.get(1) - removedItem.def);
            stats.set(2, stats.get(2) - removedItem.vit);
            stats.set(3, stats.get(3) - removedItem.dmg);
            stats.set(4, stats.get(4) - removedItem.luck);
        }

        // Add stats upon adding item //
        if (newItem != null) {
            stats.set(0, stats.get(0) + newItem.atk);
            stats.set(1, stats.get(1) + newItem.def);
            stats.set(2, stats.get(2) + newItem.vit);
            stats.set(3, stats.get(3) + newItem.dmg);
            stats.set(4, stats.get(4) + newItem.luck);
        }

        atkText.setText(String.format(Locale.ENGLISH, "%,d", stats.get(0)));
        defText.setText(String.format(Locale.ENGLISH, "%,d", stats.get(1)));
        vitText.setText(String.format(Locale.ENGLISH, "%,d", stats.get(2)));
        dmgText.setText(String.format(Locale.ENGLISH, "%,d", stats.get(3)));
        luckText.setText(String.format(Locale.ENGLISH, "%,d", stats.get(4)));
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
                intent.putExtra("atk", stats.get(0));
                intent.putExtra("def", stats.get(1));
                intent.putExtra("vit", stats.get(2));
                intent.putExtra("dmg", stats.get(3));
                intent.putExtra("luck", stats.get(4));
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
        findViewById(R.id.eq_bracers).setOnLongClickListener(this);
        findViewById(R.id.eq_bracers).setOnDragListener(this);
        findViewById(R.id.eq_bracers).setOnClickListener(this::openTooltipListener);
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
                } else if (ownerItem.lvlReq > level) {
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
                } else if (ownerItem.lvlReq > level) {
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

        if (item.dmg > 0) {
            findViewById(R.id.itemTooltipDmgLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.itemTooltipDmg)).setText("+" + item.dmg);
        } else
            findViewById(R.id.itemTooltipDmgLayout).setVisibility(View.GONE);

        if (item.luck > 0) {
            findViewById(R.id.itemTooltipLuckLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.itemTooltipLuck)).setText("+" + item.luck);
        } else
            findViewById(R.id.itemTooltipLuckLayout).setVisibility(View.GONE);

        // Show Sell Button if Bag Item //
        if (viewId.substring(0, 3).equals("bag"))
            findViewById(R.id.itemTooltipSell).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.itemTooltipSell).setVisibility(View.GONE);


        findViewById(R.id.itemTooltipLayout).setVisibility(View.VISIBLE);
    }

    public void closeTooltipListener(View view) {
        int id = view.getId();
        if (id == R.id.itemTooltipLayout || id == R.id.itemTooltipClose)
            findViewById(R.id.itemTooltipLayout).setVisibility(View.GONE);
    }

    public void sellItemListener(View view) {
        Toast.makeText(this, "Selling item.", Toast.LENGTH_SHORT).show();
    }
}
