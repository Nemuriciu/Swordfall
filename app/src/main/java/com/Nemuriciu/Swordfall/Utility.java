package com.Nemuriciu.Swordfall;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class Utility {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static String loadJSONFromAsset(String filename, Context context) {
        String json;

        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }

    static int getEqArrayIndex(String type) {
        switch (type) {
            case "helm":
                return 0;
            case "gloves":
                return 1;
            case "chest":
                return 2;
            case "bracers":
                return 3;
            case "weapon":
                return 4;
            case "pants":
                return 5;
            case "offhand":
                return 6;
            case "boots":
                return 7;

            default:
                return -1;
        }
    }

    static double getNextLevelExp(long nextLevel) {
        return 1445 * Math.pow(nextLevel, 2) - 250 * nextLevel;
    }
}
