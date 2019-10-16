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

import java.util.HashMap;
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
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
        Map<String, Object> info = new HashMap<>();

        info.put("level", 1);
        info.put("gold", 0);
        info.put("currentExp", 0);
        info.put("maxExp", 100);
        info.put("currentStamina", 25);
        info.put("maxStamina", 25);

        data.put("hasCharacter", true);
        data.put("username", username);
        data.put("class", selectedClass);
        data.put("info", info);

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
