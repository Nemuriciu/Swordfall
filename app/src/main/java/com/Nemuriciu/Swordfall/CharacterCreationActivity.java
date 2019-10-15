package com.Nemuriciu.Swordfall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class CharacterCreationActivity extends AppCompatActivity {

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_creation);

        Bundle extras = getIntent().getExtras();

        if(extras != null)
            uid = extras.getString("uid");

        Toast.makeText(this, uid, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> CharacterCreationActivity.super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
    }
}
