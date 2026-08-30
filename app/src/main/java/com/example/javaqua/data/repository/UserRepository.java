package com.example.javaqua.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

    public UserRepository() {
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        userLiveData.setValue(mAuth.getCurrentUser());
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return userLiveData;
    }

    public void updateUsername(String newUsername, RepositoryCallback<Void> callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onError(new Exception("No user logged in"));
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                db.collection("users").document(user.getUid())
                        .update("username", newUsername)
                        .addOnSuccessListener(aVoid -> {
                            userLiveData.setValue(mAuth.getCurrentUser());
                            callback.onSuccess(null);
                        })
                        .addOnFailureListener(callback::onError);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void updateEmail(String newEmail, String currentEmail, String password, RepositoryCallback<Void> callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);
        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                user.updateEmail(newEmail).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        userLiveData.setValue(mAuth.getCurrentUser());
                        callback.onSuccess(null);
                    } else {
                        callback.onError(updateTask.getException());
                    }
                });
            } else {
                callback.onError(reauthTask.getException());
            }
        });
    }

    public void sendPasswordResetEmail(String email, RepositoryCallback<Void> callback) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void signOut() {
        mAuth.signOut();
        userLiveData.setValue(null);
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
