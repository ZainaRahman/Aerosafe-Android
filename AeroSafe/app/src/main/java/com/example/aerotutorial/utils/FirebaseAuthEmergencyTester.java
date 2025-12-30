package com.example.aerotutorial.utils;

import android.app.Activity;
import android.widget.Toast;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

/**
 * Emergency Firebase Authentication Tester
 * Use this class to test and fix the "setup issue" error
 */
public class FirebaseAuthEmergencyTester {

    private static final String TAG = "FirebaseEmergencyTest";
    private Activity activity;
    private FirebaseAuth auth;

    public FirebaseAuthEmergencyTester(Activity activity) {
        this.activity = activity;
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Run comprehensive Firebase auth tests
     * Call this from your LoginActivity onCreate
     */
    public void runEmergencyTests() {
        Log.d(TAG, "🚨 RUNNING EMERGENCY FIREBASE AUTH TESTS 🚨");

        testBasicFirebaseConnection();
        testEmailPasswordCreation();
        testSpecificAuthErrors();
    }

    private void testBasicFirebaseConnection() {
        Log.d(TAG, "Testing basic Firebase connection...");

        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            Log.d(TAG, "✅ Firebase Auth instance created successfully");

            if (auth.getCurrentUser() != null) {
                Log.d(TAG, "✅ Current user: " + auth.getCurrentUser().getEmail());
            } else {
                Log.d(TAG, "ℹ️  No user currently signed in (this is normal)");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Firebase Auth initialization failed", e);
            showToast("Firebase initialization failed: " + e.getMessage());
        }
    }

    private void testEmailPasswordCreation() {
        Log.d(TAG, "Testing Email/Password authentication availability...");

        // Test with invalid email to see specific error response
        String testEmail = "test" + System.currentTimeMillis() + "@example.com";
        String testPassword = "testpass123";

        auth.createUserWithEmailAndPassword(testEmail, testPassword)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "✅ Test account created successfully! Deleting...");
                    showToast("✅ Firebase Auth is working correctly!");

                    // Delete the test user immediately
                    if (auth.getCurrentUser() != null) {
                        auth.getCurrentUser().delete()
                            .addOnCompleteListener(deleteTask -> {
                                Log.d(TAG, "Test user deleted");
                                auth.signOut();
                            });
                    }
                } else {
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthException) {
                        FirebaseAuthException authException = (FirebaseAuthException) e;
                        String errorCode = authException.getErrorCode();

                        Log.e(TAG, "Firebase Auth Error Code: " + errorCode);
                        Log.e(TAG, "Firebase Auth Error Message: " + e.getMessage());

                        analyzeAuthError(errorCode, e.getMessage());
                    } else {
                        Log.e(TAG, "Non-Firebase Auth error", e);
                        showToast("Unknown error: " + e.getMessage());
                    }
                }
            });
    }

    private void analyzeAuthError(String errorCode, String message) {
        Log.d(TAG, "🔍 ANALYZING AUTH ERROR: " + errorCode);

        switch (errorCode) {
            case "ERROR_OPERATION_NOT_ALLOWED":
                Log.e(TAG, "❌❌❌ EMAIL/PASSWORD AUTHENTICATION IS DISABLED! ❌❌❌");
                Log.e(TAG, "SOLUTION: Go to Firebase Console → Authentication → Sign-in method → Enable Email/Password");
                showToast("🚨 SETUP ISSUE: Enable Email/Password in Firebase Console!");
                break;

            case "ERROR_WEAK_PASSWORD":
                Log.d(TAG, "✅ Firebase Auth is enabled (got weak password error - this is expected)");
                showToast("✅ Firebase Auth is working! Try a stronger password.");
                break;

            case "ERROR_INVALID_EMAIL":
                Log.d(TAG, "✅ Firebase Auth is enabled (got invalid email error - this is expected)");
                showToast("✅ Firebase Auth is working! Check your email format.");
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                Log.d(TAG, "✅ Firebase Auth is enabled (email already exists - this is expected)");
                showToast("✅ Firebase Auth is working! Email already registered.");
                break;

            default:
                Log.w(TAG, "⚠️  Unknown error code: " + errorCode);
                Log.w(TAG, "Message: " + message);

                if (message.contains("setup") || message.contains("administrator") ||
                    message.contains("not enabled") || message.contains("disabled")) {
                    Log.e(TAG, "❌❌❌ SETUP ISSUE DETECTED! ❌❌❌");
                    showToast("🚨 SETUP ISSUE: Check Firebase Console configuration!");
                } else {
                    showToast("Unknown Firebase error: " + errorCode);
                }
                break;
        }
    }

    private void testSpecificAuthErrors() {
        Log.d(TAG, "Testing specific auth scenarios...");

        // Test with completely invalid email to trigger specific errors
        auth.createUserWithEmailAndPassword("invalid-email", "short")
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() instanceof FirebaseAuthException) {
                    FirebaseAuthException e = (FirebaseAuthException) task.getException();
                    Log.d(TAG, "Invalid email test - Error: " + e.getErrorCode());
                }
            });
    }

    private void showToast(String message) {
        if (activity != null) {
            activity.runOnUiThread(() ->
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
            );
        }
    }

    /**
     * Test sign-in with existing account
     */
    public void testSignIn(String email, String password) {
        Log.d(TAG, "Testing sign-in with: " + email);

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "✅ Sign-in successful!");
                    showToast("✅ Login successful!");
                } else {
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthException) {
                        FirebaseAuthException authException = (FirebaseAuthException) e;
                        analyzeAuthError(authException.getErrorCode(), e.getMessage());
                    }
                }
            });
    }
}
