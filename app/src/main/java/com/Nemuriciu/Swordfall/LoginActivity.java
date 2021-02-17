package com.Nemuriciu.Swordfall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private TextInputEditText email;
    private TextInputEditText pass;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPass;
    private AppCompatButton loginButton;
    private AppCompatButton registerButton;
    private TextView forgotPass;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = currentUser.getUid();

            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    assert doc != null;
                    if (doc.exists()) {
                        boolean res = (boolean) Objects.requireNonNull(doc.getData()).get("hasCharacter");

                        Intent intent;
                        if (res) {
                            intent = new Intent(this, MainActivity.class);
                            Stats stats = doc.get("stats", Stats.class);

                            intent.putExtra("username", (String)doc.get("username"));
                            intent.putExtra("level", (long)doc.get("level"));
                            intent.putExtra("gold", (long)doc.get("gold"));
                            intent.putExtra("currentStamina", (long)doc.get("currentStamina"));
                            intent.putExtra("maxStamina", (long)doc.get("maxStamina"));
                            intent.putExtra("currentExp", (long)doc.get("currentExp"));
                            intent.putExtra("maxExp", (long)doc.get("maxExp"));
                            intent.putExtra("stats", stats);
                            intent.putStringArrayListExtra("eqSlots",
                                    (ArrayList<String>) doc.get("eqSlots"));
                            intent.putStringArrayListExtra("bagSlots",
                                    (ArrayList<String>) doc.get("bagSlots"));
                        }
                        else
                            intent = new Intent(this, CharacterCreationActivity.class);


                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("uid", uid);
                        startActivity(intent);
                    }
                } else
                    Log.d(TAG, "DocRef get failed with ", task.getException());
            });
        } else {
            email.setAlpha(1.0f);
            layoutEmail.setAlpha(1.0f);
            pass.setAlpha(1.0f);
            layoutPass.setAlpha(1.0f);
            loginButton.setAlpha(1.0f);
            registerButton.setAlpha(1.0f);
            forgotPass.setAlpha(1.0f);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.loginEmail);
        layoutEmail = findViewById(R.id.loginLayoutEmail);
        pass = findViewById(R.id.loginPass);
        layoutPass = findViewById(R.id.loginLayoutPass);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        forgotPass = findViewById(R.id.loginForgotPass);

        email.setAlpha(0);
        layoutEmail.setAlpha(0);
        pass.setAlpha(0);
        layoutPass.setAlpha(0);
        loginButton.setAlpha(0);
        registerButton.setAlpha(0);
        forgotPass.setAlpha(0);

        // Email/Pass Login
        loginButton.setOnClickListener(v -> {
            if (v.getId() == R.id.loginButton) {
                String emailText = Objects.requireNonNull(email.getText()).toString();
                String passText = Objects.requireNonNull(pass.getText()).toString();

                if (!emailText.equals("") && !passText.equals("")) {
                    mAuth.signInWithEmailAndPassword(emailText, passText)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    assert user != null;
                                    String uid = user.getUid();

                                    DocumentReference docRef = db.collection("users").document(uid);
                                    docRef.get().addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            DocumentSnapshot doc = task2.getResult();
                                            assert doc != null;
                                            if (doc.exists()) {
                                                boolean res = (boolean) Objects.requireNonNull(doc.getData()).get("hasCharacter");

                                                Intent intent;
                                                if (res) {
                                                    intent = new Intent(this, MainActivity.class);
                                                    Stats stats = doc.get("stats", Stats.class);

                                                    intent.putExtra("username", (String)doc.get("username"));
                                                    intent.putExtra("level", (long)doc.get("level"));
                                                    intent.putExtra("gold", (long)doc.get("gold"));
                                                    intent.putExtra("currentStamina", (long)doc.get("currentStamina"));
                                                    intent.putExtra("maxStamina", (long)doc.get("maxStamina"));
                                                    intent.putExtra("currentExp", (long)doc.get("currentExp"));
                                                    intent.putExtra("maxExp", (long)doc.get("maxExp"));
                                                    intent.putExtra("stats", stats);
                                                    intent.putStringArrayListExtra("eqSlots",
                                                            (ArrayList<String>) doc.get("eqSlots"));
                                                    intent.putStringArrayListExtra("bagSlots",
                                                            (ArrayList<String>) doc.get("bagSlots"));
                                                } else
                                                    intent = new Intent(this, CharacterCreationActivity.class);

                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                intent.putExtra("uid", uid);
                                                startActivity(intent);
                                            }
                                        } else
                                            Log.d(TAG, "DocRef get failed with ", task.getException());
                                    });
                                } else {
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(this, "Invalid email or password.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        registerButton.setOnClickListener(v -> {
            if (v.getId() == R.id.registerButton)
                createAccount();
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> LoginActivity.super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
    }

    private void createAccount() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
