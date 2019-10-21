package com.Nemuriciu.Swordfall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CharacterCreationActivity extends AppCompatActivity {

    private static final String TAG = "CharCreation";
    private String uid;

    private String[] _class = {"FIGHTER", "RANGER", "WIZARD"};
    private int currentIndex;
    private String currentSelection;

    private TextView selectedClass;
    private ImageView avatar;
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

        selectedClass = findViewById(R.id.charCreationClass);
        avatar = findViewById(R.id.characterCreationAvatar);
        usernameInput = findViewById(R.id.charCreationInputText);
        ImageView leftArrow = findViewById(R.id.charCreationLeft);
        ImageView rightArrow = findViewById(R.id.charCreationRight);
        Button confirm = findViewById(R.id.charCreationCreateButton);

        currentSelection = _class[currentIndex];
        selectedClass.setText(currentSelection);

        leftArrow.setOnClickListener(v -> {
            if (v.getId() == R.id.charCreationLeft) {
                switchSelection("prev");
            }
        });

        rightArrow.setOnClickListener(v -> {
            if (v.getId() == R.id.charCreationRight) {
                switchSelection("next");
            }
        });

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
                                createUser(uid, username, currentSelection);

                                Intent intent = new Intent(this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("uid", uid);
                                intent.putExtra("username", username);
                                intent.putExtra("class", currentSelection);
                                intent.putExtra("level", (long)1);
                                intent.putExtra("gold", (long)1250);
                                intent.putExtra("currentStamina", (long)25);
                                intent.putExtra("maxStamina", (long)25);
                                intent.putExtra("currentExp", (long)0);
                                intent.putExtra("maxExp", (long)100);
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

    private void createUser(String uid, String username, String selectedClass) {
        Map<String, Object> data = new HashMap<>();

        data.put("hasCharacter", true);
        data.put("username", username);
        data.put("class", selectedClass);

        data.put("level", 1);
        data.put("gold", 1250);
        data.put("currentExp", 0);
        data.put("maxExp", 100);
        data.put("currentStamina", 25);
        data.put("maxStamina", 25);

        List<Integer> zoneDepth = new ArrayList<>(Collections.nCopies(5, 1));
        data.put("zoneDepth", zoneDepth);

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

    private void switchSelection(String dir) {
        if (dir.equals("prev")) {
            if (currentIndex == 0)
                currentIndex = _class.length - 1;
            else
                currentIndex--;
        } else if (dir.equals("next")) {
            if (currentIndex == _class.length - 1)
                currentIndex = 0;
            else
                currentIndex++;
        }

        currentSelection = _class[currentIndex];

        switch (currentSelection) {
            case "FIGHTER":
                avatar.setImageResource(R.drawable.character_creation_avatar_fighter);
                break;
            case "RANGER":
                avatar.setImageResource(R.drawable.character_creation_avatar_ranger);
                break;
            case "WIZARD":
                avatar.setImageResource(R.drawable.character_creation_avatar_wizard);
                break;
        }

        selectedClass.setText(currentSelection);
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
