package com.Nemuriciu.Swordfall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    private TextInputEditText email;
    private TextInputEditText pass;
    private TextInputEditText passConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.close_button);

        email = findViewById(R.id.input_email);
        pass = findViewById(R.id.input_password);
        passConfirm = findViewById(R.id.input_confirm_password);

        AppCompatButton confirmButton = findViewById(R.id.createAccount);

        confirmButton.setOnClickListener(v -> {
            if (v.getId() == R.id.createAccount) {
                boolean res = validate();
                String emailText = Objects.requireNonNull(email.getText()).toString();
                String passText = Objects.requireNonNull(pass.getText()).toString();
                
                if (res) {
                    mAuth.createUserWithEmailAndPassword(emailText, passText)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    assert user != null;
                                    String uid = user.getUid();
                                    // Add uid to cloud
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("hasCharacter", false);
                                    data.put("email", emailText);

                                    db.collection("users").document(uid)
                                            .set(data)
                                            .addOnFailureListener(e -> Log.w(TAG, "Error writing document.", e));

                                    Intent intent = new Intent(this, CharacterCreationActivity.class);
                                    intent.putExtra("uid", user.getUid());
                                    startActivity(intent);
                                } else {
                                    if (task.getException() != null)
                                        try {
                                            throw task.getException();
                                        } catch(FirebaseAuthUserCollisionException e) {
                                            Toast.makeText(this, "Email Address already in use.",
                                                    Toast.LENGTH_SHORT).show();
                                        } catch(Exception e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                }
                            });
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private boolean validate() {
        boolean flag = true;
        String checkEmail = Objects.requireNonNull(email.getText()).toString();
        String password = Objects.requireNonNull(pass.getText()).toString();
        String confPass = Objects.requireNonNull(passConfirm.getText()).toString();

        if (!EMAIL_PATTERN.matcher(checkEmail).matches()) {
            Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
            flag = false;
        } else if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            flag = false;
        } else if (!password.equals(confPass)){
            Toast.makeText(this,"Password Not Matching",Toast.LENGTH_SHORT).show();
            flag = false;
        }

        return flag;
    }
}
