package com.Nemuriciu.Swordfall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CharacterCreationActivity extends AppCompatActivity {

    private static final String TAG = "CharCreation";
    private String uid;

    private TextInputEditText usernameInput;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_creation);
        db = FirebaseFirestore.getInstance();

        Bundle extras = getIntent().getExtras();

        assert extras != null;
        uid = extras.getString("uid");

        usernameInput = findViewById(R.id.charCreationInputText);
        Button confirm = findViewById(R.id.charCreationCreateButton);

        confirm.setOnClickListener(v -> {
            if (v.getId() == R.id.charCreationCreateButton) {
                String username = Objects.requireNonNull(usernameInput.getText()).toString();

                if (validate(username)) {
                    DocumentReference docRef = db.collection("usersByName").document(username);
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            assert document != null;
                            if (document.exists()) {
                                Toast.makeText(this, "Username already exists.", Toast.LENGTH_SHORT).show();
                            } else {
                                Map<String, Object> data = new HashMap<>();
                                data.put("uid", uid);

                                // Add username to usersByName collection
                                db.collection("usersByName").document(username).set(data);
                                createUser(uid, username);

                                Intent intent = new Intent(this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("uid", uid);
                                intent.putExtra("username", username);
                                intent.putExtra("level", (long)1);
                                intent.putExtra("gold", (long)100);
                                intent.putExtra("currentStamina", (long)100);
                                intent.putExtra("maxStamina", (long)100);
                                intent.putExtra("currentExp", (long)0);
                                intent.putExtra("maxExp", (long)2500);
                                intent.putExtra("stats", new Stats(12, 12, 12, 4, 6, 10, 0));
                                intent.putStringArrayListExtra("eqSlots",
                                        new ArrayList<>(Collections.nCopies(8, "0")));
                                intent.putStringArrayListExtra("bagSlots",
                                        new ArrayList<>(Collections.nCopies(56, "0")));
                                startActivity(intent);
                            }
                        } else {
                            Log.d(TAG, "Failed with: ", task.getException());
                        }
                    });
                }
            }
        });
    }

    private void createUser(String uid, String username) {
        Map<String, Object> data = new HashMap<>();

        data.put("hasCharacter", true);
        data.put("username", username);

        data.put("level", 1);
        data.put("gold", 100);
        data.put("currentExp", 0);
        data.put("maxExp", 2500);
        data.put("currentStamina", 100);
        data.put("maxStamina", 100);

        data.put("zoneDepth", Arrays.asList(1, 1, 1, 1, 1));

        data.put("stats", new Stats(12, 12, 12, 4, 6, 10, 0));
        data.put("eqSlots", Collections.nCopies(8, "0"));
        data.put("bagSlots", Collections.nCopies(56, "0"));

        db.collection("users").document(uid)
                .update(data)
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));
    }

    private boolean validate(String username) {
        if (username.length() < 3) {
            Toast.makeText(this, "Username must be between 3 and 16 characters.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!username.matches("[a-zA-Z]+[a-zA-Z0-9]+")) {
            Toast.makeText(this, "Username must be alphanumeric.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
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
