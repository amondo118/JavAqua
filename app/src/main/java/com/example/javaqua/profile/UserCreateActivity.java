package com.example.javaqua.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.javaqua.R;
import com.example.javaqua.main.MainActivity;
import com.example.javaqua.main.SignInActivity;
import com.example.javaqua.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserCreateActivity extends AppCompatActivity {

    private static final String TAG = "UserCreateActivity";

    private EditText editTextUsername;
    private Button buttonCreateUser;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String email;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_create);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {


            Log.e(TAG, "User is null in UserCreateActivity, redirecting to sign-in.");
            Toast.makeText(this, "Error: User session not found. Please sign in again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        email = currentUser.getEmail();
        userId = currentUser.getUid();

        editTextUsername = findViewById(R.id.usernameEditText);
        buttonCreateUser = findViewById(R.id.submitButton);





        buttonCreateUser.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            if (username.isEmpty()) {
                editTextUsername.setError("Username cannot be empty");
                editTextUsername.requestFocus();
                return;
            }
            createUser(username);
        });
    }

    private void createUser(String username) {
        if (userId == null || email == null) {
            Log.e(TAG, "Cannot create user, userId or email is null.");
            Toast.makeText(this, "Error: User information is missing.", Toast.LENGTH_SHORT).show();
            // Optionally, redirect to sign-in again or handle appropriately
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        User user = new User(userId, email, username);

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile created successfully for: " + userId);
                    Toast.makeText(UserCreateActivity.this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(UserCreateActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user profile for: " + userId, e);
                    Toast.makeText(UserCreateActivity.this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
    }
}
