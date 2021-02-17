package com.Nemuriciu.Swordfall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class Item {

    String id, name, avatar, type, rarity;
    long lvlReq, atk, def, vit, dmgMin, dmgMax, luck, sellPrice;
    private final JSONObject items;

    Item(String id, JSONObject items) {
        this.id = id;
        this.items = items;
        try {
            fetchItem();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchItem() throws JSONException {
        JSONObject item = items.getJSONObject(id);
        JSONArray dmg = item.getJSONArray("dmg");

        type = item.getString("type");
        name = item.getString("name");
        avatar = item.getString("avatar");
        rarity = item.getString("rarity");
        lvlReq = item.getInt("levelReq");
        atk = item.getInt("atk");
        def = item.getInt("def");
        vit = item.getInt("vit");
        dmgMin = dmg.getInt(0);
        dmgMax = dmg.getInt(1);
        luck = item.getInt("luck");
        sellPrice = item.getInt("sellPrice");
    }
}
