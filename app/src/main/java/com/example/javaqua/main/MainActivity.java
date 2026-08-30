package com.example.javaqua.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.example.javaqua.R;
import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.AtlasFragment;
import com.example.javaqua.forum.ForumFragment;
import com.example.javaqua.myaqua.AquariumSync;
import com.example.javaqua.myaqua.MyAquariumFragment;
import com.example.javaqua.profile.SettingsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private static final String PREFS_NAME = "login_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_THEME = "app_theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedTheme = sharedPrefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        // Immediate check: If no user, go to SignInActivity and stop here
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            saveLoginState(false, null);
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // If we reach here, user is logged in
        saveLoginState(true, currentUser.getUid());
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // UI Adjustments
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Sync and Load Data
        new Atlas(getApplicationContext()).refreshFishPreviewsFromFirestoreAndSaveToRoom();
        AquariumSync.syncAquariums(getApplicationContext(), currentUser.getUid());

        // Load Initial Fragment
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView5, MyAquariumFragment.class, null)
                    .commit();
        }
    }

    private void saveLoginState(boolean isLoggedIn, String uid) {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sharedPref.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .putString(KEY_USER_UID, uid)
                .apply();
    }

    // Fragment Navigation Methods
    public void onButton1(View view) {
        switchFragment(new MyAquariumFragment());
    }

    public void onButton2(View view) {
        switchFragment(new AtlasFragment());
    }

    public void onButton3(View view) {
        switchFragment(new ForumFragment());
    }

    public void onButton4(View view) {
        switchFragment(new SettingsFragment());
    }

    private void switchFragment(androidx.fragment.app.Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView5, fragment)
                .setReorderingAllowed(true)
                .commit();
    }
}
