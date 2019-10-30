package com.Nemuriciu.Swordfall;

import org.json.JSONException;
import org.json.JSONObject;

class Item {

    String id, name, avatar, type, rarity;
    long lvlReq, atk, def, vit, dmg, luck;
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

        type = item.getString("type");
        name = item.getString("name");
        avatar = item.getString("avatar");
        rarity = item.getString("rarity");
        lvlReq = item.getInt("levelReq");
        atk = item.getInt("atk");
        def = item.getInt("def");
        vit = item.getInt("vit");
        dmg = item.getInt("dmg");
        luck = item.getInt("luck");
    }
}
