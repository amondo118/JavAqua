package com.example.javaqua.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaqua.R;
import com.example.javaqua.databinding.FragmentSettingsBinding;
import com.example.javaqua.main.SignInActivity;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private static final String PREFS_NAME = "login_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_THEME = "app_theme";

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private SharedPreferences sharedPrefs;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        sharedPrefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.button2.setOnClickListener(v -> onLogOut());
        binding.button5.setOnClickListener(v -> showUpdateUsernameDialog());
        binding.button4.setOnClickListener(v -> showUpdateEmailDialog());
        binding.button6.setOnClickListener(v -> showResetPasswordDialog());

        setupThemeSelection();
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupThemeSelection() {
        int currentTheme = sharedPrefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.radioDark.setChecked(true);
        } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            binding.radioLight.setChecked(true);
        } else {
            binding.radioSystem.setChecked(true);
        }

        binding.themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int newTheme;
            if (checkedId == R.id.radioLight) {
                newTheme = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.radioDark) {
                newTheme = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                newTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }

            sharedPrefs.edit().putInt(KEY_THEME, newTheme).apply();
            AppCompatDelegate.setDefaultNightMode(newTheme);
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            binding.usernameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "No username set");
            binding.emailText.setText(user.getEmail());
        }
    }

    private void showUpdateUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Update Username");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("New username");
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                viewModel.updateUsername(newUsername);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showUpdateEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Update Email");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("New email");
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newEmail = input.getText().toString().trim();
            if (!newEmail.isEmpty()) {
                showReauthDialog((email, password) -> viewModel.updateEmail(newEmail, email, password));
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showResetPasswordDialog() {
        FirebaseUser user = viewModel.getCurrentUser().getValue();
        if (user == null || user.getEmail() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reset Password");
        builder.setMessage("Send a password reset email to " + user.getEmail() + "?");

        builder.setPositiveButton("Send", (dialog, which) -> viewModel.sendPasswordResetEmail(user.getEmail()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private interface ReauthCallback {
        void onResult(String email, String password);
    }

    private void showReauthDialog(ReauthCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Re-authenticate");
        builder.setMessage("Please enter your current credentials to proceed.");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reauth, null);
        final EditText emailInput = view.findViewById(R.id.reauth_email);
        final EditText passwordInput = view.findViewById(R.id.reauth_password);
        
        FirebaseUser user = viewModel.getCurrentUser().getValue();
        if (user != null) {
            emailInput.setText(user.getEmail());
        }
        
        builder.setView(view);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            callback.onResult(emailInput.getText().toString(), passwordInput.getText().toString());
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void onLogOut() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USER_UID);
        editor.apply();

        viewModel.signOut();

        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
