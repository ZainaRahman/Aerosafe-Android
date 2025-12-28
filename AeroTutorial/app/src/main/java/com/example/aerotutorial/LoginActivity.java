package com.example.aerotutorial;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aerotutorial.databinding.ActivityLoginBinding;
import com.example.aerotutorial.models.User;
import com.example.aerotutorial.repository.AuthRepository;
import com.example.aerotutorial.utils.PreferencesManager;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthRepository authRepository;
    private PreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository();
        prefsManager = new PreferencesManager(this);

        // Check if user is already logged in
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            String role = prefsManager.getUserRole();
            navigateToDashboard(role);
            return;
        }

        setupListeners();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> loginUser());

        binding.tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        binding.tvForgotPassword.setOnClickListener(v -> resetPassword());
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required");
            return;
        }

        showLoading(true);

        authRepository.signIn(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                showLoading(false);

                // Save user info to preferences
                prefsManager.saveUserInfo(
                    user.getId(),
                    user.getRole(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getLocation()
                );

                Toast.makeText(LoginActivity.this,
                    "Welcome, " + user.getUsername(), Toast.LENGTH_SHORT).show();
                navigateToDashboard(user.getRole());
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetPassword() {
        String email = binding.etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        authRepository.resetPassword(email, task -> {
            showLoading(false);
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this,
                    "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this,
                    "Failed to send reset email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent;

        switch (role) {
            case "researcher":
                intent = new Intent(this, ResearcherDashboardActivity.class);
                break;
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case "user":
            default:
                intent = new Intent(this, UserDashboardActivity.class);
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!show);
    }
}

