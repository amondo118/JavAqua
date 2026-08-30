package com.example.javaqua.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.javaqua.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(mAuth.getCurrentUser().getUid(), email);

                    }
                });
    }

    private void saveUserToFirestore(String uid, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User user = new User(uid, email);
        db.collection("users").document(uid).set(user);
    }
}