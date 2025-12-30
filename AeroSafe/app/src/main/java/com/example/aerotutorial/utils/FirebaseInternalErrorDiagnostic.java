package com.example.aerotutorial.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

/**
 * Advanced Firebase Internal Error Diagnostics
 * Specifically designed to diagnose "internal error" issues
 */
public class FirebaseInternalErrorDiagnostic {

    private static final String TAG = "FirebaseInternalDiag";
    private Activity activity;

    public FirebaseInternalErrorDiagnostic(Activity activity) {
        this.activity = activity;
    }

    public void runInternalErrorDiagnostics() {
        Log.d(TAG, "🚨 RUNNING INTERNAL ERROR DIAGNOSTICS 🚨");

        checkGooglePlayServices();
        checkNetworkConnectivity();
        checkFirebaseInitialization();
        checkApiKeyValidity();
        testSpecificInternalErrorScenarios();
    }

    private void checkGooglePlayServices() {
        Log.d(TAG, "Checking Google Play Services...");

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(activity);

        if (result != ConnectionResult.SUCCESS) {
            Log.e(TAG, "❌ Google Play Services issue: " + result);

            if (googleAPI.isUserResolvableError(result)) {
                Log.e(TAG, "Google Play Services error is user resolvable");
                showToast("❌ Google Play Services needs update!");
            } else {
                Log.e(TAG, "Google Play Services error is NOT resolvable");
                showToast("❌ Google Play Services not available!");
            }
        } else {
            Log.d(TAG, "✅ Google Play Services is available and up to date");
        }
    }

    private void checkNetworkConnectivity() {
        Log.d(TAG, "Checking network connectivity...");

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            Log.d(TAG, "✅ Network is connected: " + activeNetwork.getTypeName());
        } else {
            Log.e(TAG, "❌ No network connection available");
            showToast("❌ No internet connection!");
        }
    }

    private void checkFirebaseInitialization() {
        Log.d(TAG, "Checking Firebase initialization...");

        try {
            // Check if Firebase is initialized
            FirebaseApp app = FirebaseApp.getInstance();
            Log.d(TAG, "✅ Firebase App initialized: " + app.getName());
            Log.d(TAG, "   Project ID: " + app.getOptions().getProjectId());
            Log.d(TAG, "   Application ID: " + app.getOptions().getApplicationId());
            Log.d(TAG, "   API Key: " + app.getOptions().getApiKey().substring(0, 10) + "...");

            // Check FirebaseAuth specifically
            FirebaseAuth auth = FirebaseAuth.getInstance(app);
            Log.d(TAG, "✅ FirebaseAuth instance created from app");

        } catch (Exception e) {
            Log.e(TAG, "❌ Firebase initialization error", e);
            showToast("❌ Firebase initialization failed: " + e.getMessage());
        }
    }

    private void checkApiKeyValidity() {
        Log.d(TAG, "Testing API key validity...");

        // Test a simple Firebase operation to validate API key
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Try to send password reset to a dummy email - this will validate API key
        auth.sendPasswordResetEmail("test@nonexistentdomain12345.com")
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthException) {
                        FirebaseAuthException authException = (FirebaseAuthException) e;
                        String errorCode = authException.getErrorCode();

                        Log.d(TAG, "API Key test error code: " + errorCode);

                        switch (errorCode) {
                            case "ERROR_INVALID_EMAIL":
                            case "ERROR_USER_NOT_FOUND":
                                Log.d(TAG, "✅ API Key is valid (got expected user-not-found error)");
                                showToast("✅ API Key is working!");
                                break;

                            case "ERROR_INVALID_API_KEY":
                                Log.e(TAG, "❌ INVALID API KEY!");
                                showToast("❌ Invalid API Key - redownload google-services.json!");
                                break;

                            case "ERROR_NETWORK_REQUEST_FAILED":
                                Log.e(TAG, "❌ Network request failed");
                                showToast("❌ Network issue - check internet connection!");
                                break;

                            case "ERROR_TOO_MANY_REQUESTS":
                                Log.w(TAG, "⚠️  Too many requests - API key might be restricted");
                                showToast("⚠️ Too many requests - try again later");
                                break;

                            case "ERROR_OPERATION_NOT_ALLOWED":
                                Log.e(TAG, "❌ Password reset not enabled in Firebase Console");
                                showToast("❌ Email/Password auth not enabled!");
                                break;

                            default:
                                Log.e(TAG, "❌ Unknown API key test error: " + errorCode);
                                Log.e(TAG, "   Message: " + e.getMessage());

                                if (e.getMessage().contains("INTERNAL_ERROR") ||
                                    e.getMessage().contains("internal error")) {
                                    Log.e(TAG, "🚨 INTERNAL ERROR DETECTED!");
                                    analyzeInternalError(e.getMessage());
                                }
                                break;
                        }
                    } else {
                        Log.e(TAG, "Non-auth exception during API test", e);
                    }
                } else {
                    Log.d(TAG, "Unexpected success in API key test");
                }
            });
    }

    private void testSpecificInternalErrorScenarios() {
        Log.d(TAG, "Testing specific internal error scenarios...");

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Test 1: Try creating user with simple credentials
        String testEmail = "internaltest" + System.currentTimeMillis() + "@test.com";
        auth.createUserWithEmailAndPassword(testEmail, "password123")
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthException) {
                        FirebaseAuthException authException = (FirebaseAuthException) e;
                        String errorCode = authException.getErrorCode();
                        String message = e.getMessage();

                        Log.d(TAG, "Create user test - Error code: " + errorCode);
                        Log.d(TAG, "Create user test - Message: " + message);

                        if (message.contains("INTERNAL_ERROR") || message.contains("internal error")) {
                            Log.e(TAG, "🚨 INTERNAL ERROR IN CREATE USER!");
                            analyzeInternalError(message);
                        } else if (errorCode.equals("ERROR_OPERATION_NOT_ALLOWED")) {
                            Log.d(TAG, "✅ Got expected 'operation not allowed' - auth not enabled");
                            showToast("Email/Password auth not enabled - enable it in Firebase Console!");
                        }
                    }
                } else {
                    Log.d(TAG, "✅ Create user succeeded - deleting test user");
                    if (auth.getCurrentUser() != null) {
                        auth.getCurrentUser().delete();
                        auth.signOut();
                    }
                }
            });
    }

    private void analyzeInternalError(String errorMessage) {
        Log.e(TAG, "🔍 ANALYZING INTERNAL ERROR");
        Log.e(TAG, "Error message: " + errorMessage);

        if (errorMessage.contains("API key")) {
            Log.e(TAG, "CAUSE: API Key issue");
            showToast("🚨 Internal Error: API Key problem - redownload google-services.json!");
        } else if (errorMessage.contains("project")) {
            Log.e(TAG, "CAUSE: Project configuration issue");
            showToast("🚨 Internal Error: Firebase project issue - check project status!");
        } else if (errorMessage.contains("network") || errorMessage.contains("connection")) {
            Log.e(TAG, "CAUSE: Network connectivity issue");
            showToast("🚨 Internal Error: Network problem - check internet connection!");
        } else if (errorMessage.contains("service") || errorMessage.contains("backend")) {
            Log.e(TAG, "CAUSE: Firebase service issue");
            showToast("🚨 Internal Error: Firebase service temporarily down!");
        } else {
            Log.e(TAG, "CAUSE: Unknown internal error");
            showToast("🚨 Internal Error: Unknown cause - check Firebase Console!");
        }

        // Provide specific solutions
        Log.e(TAG, "SOLUTIONS:");
        Log.e(TAG, "1. Redownload google-services.json from Firebase Console");
        Log.e(TAG, "2. Check Firebase project status in console");
        Log.e(TAG, "3. Verify API key restrictions in Google Cloud Console");
        Log.e(TAG, "4. Check if Firebase project billing is active");
        Log.e(TAG, "5. Try again after a few minutes (might be temporary)");
    }

    private void showToast(String message) {
        if (activity != null) {
            activity.runOnUiThread(() ->
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
            );
        }
    }
}
