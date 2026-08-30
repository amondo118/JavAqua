package com.example.javaqua.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.javaqua.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class SettingsViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public SettingsViewModel() {
        this.userRepository = new UserRepository();
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void updateUsername(String newUsername) {
        userRepository.updateUsername(newUsername, new UserRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                toastMessage.setValue("Username updated");
            }

            @Override
            public void onError(Exception e) {
                toastMessage.setValue("Failed to update username");
            }
        });
    }

    public void updateEmail(String newEmail, String currentEmail, String password) {
        userRepository.updateEmail(newEmail, currentEmail, password, new UserRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                toastMessage.setValue("Email updated");
            }

            @Override
            public void onError(Exception e) {
                toastMessage.setValue("Error: " + e.getMessage());
            }
        });
    }

    public void sendPasswordResetEmail(String email) {
        userRepository.sendPasswordResetEmail(email, new UserRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                toastMessage.setValue("Reset email sent");
            }

            @Override
            public void onError(Exception e) {
                toastMessage.setValue("Error sending reset email");
            }
        });
    }

    public void signOut() {
        userRepository.signOut();
    }
}
