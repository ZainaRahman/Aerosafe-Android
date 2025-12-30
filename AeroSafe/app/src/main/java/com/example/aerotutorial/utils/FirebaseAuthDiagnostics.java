package com.example.aerotutorial.utils;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Firebase Authentication Diagnostics
 * Run this to check if Firebase is properly configured
 */
public class FirebaseAuthDiagnostics {

    private static final String TAG = "FirebaseAuthDiag";

    public static void runDiagnostics() {
        Log.d(TAG, "=== FIREBASE AUTHENTICATION DIAGNOSTICS ===");

        // Test 1: Check Firebase Auth instance
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            Log.d(TAG, "✅ FirebaseAuth instance created successfully");

            // Get current auth state
            if (auth.getCurrentUser() != null) {
                Log.d(TAG, "✅ User is currently signed in: " + auth.getCurrentUser().getEmail());
            } else {
                Log.d(TAG, "ℹ️  No user currently signed in");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ FirebaseAuth initialization failed", e);
        }

        // Test 2: Check Firebase Database instance
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            Log.d(TAG, "✅ FirebaseDatabase instance created successfully");
            Log.d(TAG, "   Database URL: " + database.getReference().toString());
        } catch (Exception e) {
            Log.e(TAG, "❌ FirebaseDatabase initialization failed", e);
        }

        // Test 3: Test email/password authentication availability
        testAuthenticationMethods();

        Log.d(TAG, "=== DIAGNOSTICS COMPLETE ===");
    }

    private static void testAuthenticationMethods() {
        Log.d(TAG, "Testing authentication methods availability...");

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Test if we can attempt to create a user (will fail but we can see the error)
        auth.createUserWithEmailAndPassword("test@example.com", "testpassword")
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "✅ Email/Password auth is working");
                    // Sign out immediately
                    auth.signOut();
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";

                    if (error.contains("EMAIL_EXISTS") || error.contains("WEAK_PASSWORD") || error.contains("INVALID_EMAIL")) {
                        Log.d(TAG, "✅ Email/Password auth is enabled (got expected validation error)");
                    } else if (error.contains("setup") || error.contains("administrator") || error.contains("not enabled")) {
                        Log.e(TAG, "❌ EMAIL/PASSWORD AUTHENTICATION NOT ENABLED IN FIREBASE CONSOLE");
                        Log.e(TAG, "   Error: " + error);
                        Log.e(TAG, "   Solution: Go to Firebase Console → Authentication → Sign-in method → Enable Email/Password");
                    } else {
                        Log.w(TAG, "⚠️  Authentication test returned: " + error);
                    }
                }
            });
    }
}
