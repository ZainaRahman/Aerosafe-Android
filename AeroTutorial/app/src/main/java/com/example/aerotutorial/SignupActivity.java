package com.example.aerotutorial;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aerotutorial.models.User;
import com.example.aerotutorial.repository.AuthRepository;
import com.example.aerotutorial.utils.PreferencesManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private TextInputEditText etUsername, etEmail, etPassword, etLocation;
    private RadioGroup rgRole;
    private MaterialButton btnSignup;
    private ProgressBar progressBar;
    private TextView tvLogin;

    private AuthRepository authRepository;
    private PreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Check Firebase initialization
        try {
            authRepository = new AuthRepository();
            prefsManager = new PreferencesManager(this);
            Log.d(TAG, "Firebase and repositories initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            Toast.makeText(this,
                "Firebase initialization error. Please check your internet connection.",
                Toast.LENGTH_LONG).show();
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etLocation = findViewById(R.id.etLocation);
        rgRole = findViewById(R.id.rgRole);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnSignup.setOnClickListener(v -> signupUser());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void signupUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return;
        }

        // Get selected role
        String role = getSelectedRole();

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(false);

        // Register user
        authRepository.signUp(email, password, username, role, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Signup successful for user: " + user.getEmail());
                progressBar.setVisibility(View.GONE);

                // Update user with location
                user.setLocation(location);

                // Save user info to preferences
                prefsManager.saveUserInfo(user.getId(), role, username, email, location);

                Toast.makeText(SignupActivity.this,
                    "Account created successfully!", Toast.LENGTH_SHORT).show();

                // Navigate to appropriate dashboard
                navigateToDashboard(role);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Signup failed with error: " + error);
                progressBar.setVisibility(View.GONE);
                btnSignup.setEnabled(true);

                // Show detailed error message
                String errorMessage = "Signup failed: " + error;
                Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                // Log to help debug
                Log.e(TAG, "Full error details: " + error);
            }
        });
    }

    private String getSelectedRole() {
        int selectedId = rgRole.getCheckedRadioButtonId();

        if (selectedId == R.id.rbUser) {
            return "user";
        } else if (selectedId == R.id.rbResearcher) {
            return "researcher";
        } else if (selectedId == R.id.rbAdmin) {
            return "admin";
        }

        return "user"; // default
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

        startActivity(intent);
        finish();
    }
}

