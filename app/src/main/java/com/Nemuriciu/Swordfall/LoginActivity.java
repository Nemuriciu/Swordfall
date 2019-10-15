package com.Nemuriciu.Swordfall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        boolean res = (boolean) Objects.requireNonNull(document.getData()).get("hasCharacter");

                        Intent intent;
                        if (res)
                            intent = new Intent(this, MainActivity.class);
                        else
                            intent = new Intent(this, CharacterCreationActivity.class);

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("uid", uid);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                } else
                    Log.d(TAG, "DocRef get failed with ", task.getException());
            });
        } else {
            email.animate().alpha(1.0f).setDuration(3000).start();
            layoutEmail.animate().alpha(1.0f).setDuration(3000).start();
            pass.animate().alpha(1.0f).setDuration(3000).start();
            layoutPass.animate().alpha(1.0f).setDuration(3000).start();
            loginButton.animate().alpha(1.0f).setDuration(3000).start();
            registerButton.animate().alpha(1.0f).setDuration(3000).start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.login_email);
        layoutEmail = findViewById(R.id.layout_email);
        pass = findViewById(R.id.login_password);
        layoutPass = findViewById(R.id.layout_pass);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);


        email.setAlpha(0);
        layoutEmail.setAlpha(0);
        pass.setAlpha(0);
        layoutPass.setAlpha(0);
        loginButton.setAlpha(0);
        registerButton.setAlpha(0);

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
                                            DocumentSnapshot document = task2.getResult();
                                            assert document != null;
                                            if (document.exists()) {
                                                boolean res = (boolean) Objects.requireNonNull(document.getData()).get("hasCharacter");

                                                Intent intent;
                                                if (res)
                                                    intent = new Intent(this, MainActivity.class);
                                                else
                                                    intent = new Intent(this, CharacterCreationActivity.class);

                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                intent.putExtra("uid", uid);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
