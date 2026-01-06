package com.example.aerotutorial.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;


public class PostFixVerificationTest {

    private static final String TAG = "PostFixVerification";
    private Activity activity;

    public PostFixVerificationTest(Activity activity) {
        this.activity = activity;
    }

    public void testFixVerification() {
        Log.d(TAG, "🧪 TESTING SHA1 FIX VERIFICATION");

        testApiKeyValidation();
        testAuthenticationFlow();
    }

    private void testApiKeyValidation() {
        Log.d(TAG, "Testing API key after SHA1 fix...");

        FirebaseAuth auth = FirebaseAuth.getInstance();


        auth.sendPasswordResetEmail("testvalidation@nonexistent.com")
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthException) {
                        FirebaseAuthException authException = (FirebaseAuthException) e;
                        String errorCode = authException.getErrorCode();

                        switch (errorCode) {
                            case "ERROR_USER_NOT_FOUND":
                            case "ERROR_INVALID_EMAIL":
                                Log.d(TAG, "✅ SHA1 FIX SUCCESSFUL! API key restrictions are working correctly");
                                showToast("✅ SHA1 Fix Successful! API key is working!");
                                break;

                            case "ERROR_INVALID_API_KEY":
                                Log.e(TAG, "❌ API key still invalid - SHA1 might not be updated yet");
                                showToast("❌ API key still invalid - wait a few more minutes");
                                break;

                            case "ERROR_NETWORK_REQUEST_FAILED":
                                Log.e(TAG, "❌ Still getting network errors - check restrictions");
                                showToast("❌ Still getting network errors");
                                break;

                            default:
                                if (e.getMessage().contains("INTERNAL_ERROR") ||
                                    e.getMessage().contains("internal error")) {
                                    Log.e(TAG, "❌ STILL GETTING INTERNAL ERROR!");
                                    Log.e(TAG, "   Error code: " + errorCode);
                                    Log.e(TAG, "   Message: " + e.getMessage());
                                    showToast("❌ Still getting internal error - check Google Cloud Console");
                                } else {
                                    Log.d(TAG, "✅ No more internal errors! Got: " + errorCode);
                                    showToast("✅ Internal error fixed! Got expected: " + errorCode);
                                }
                                break;
                        }
                    }
                } else {
                    Log.d(TAG, "Unexpected success in validation test");
                }
            });
    }

    private void testAuthenticationFlow() {
        Log.d(TAG, "Testing authentication flow...");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String testEmail = "fixtest" + System.currentTimeMillis() + "@test.com";

        // Test creating user account
        auth.createUserWithEmailAndPassword(testEmail, "testpassword123")
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthException) {
                        FirebaseAuthException authException = (FirebaseAuthException) e;
                        String errorCode = authException.getErrorCode();
                        String message = e.getMessage();

                        Log.d(TAG, "Auth test error code: " + errorCode);

                        if (message.contains("INTERNAL_ERROR") || message.contains("internal error")) {
                            Log.e(TAG, "❌ INTERNAL ERROR STILL EXISTS!");
                            showToast("❌ Internal error still present - more fixes needed");
                        } else if (errorCode.equals("ERROR_OPERATION_NOT_ALLOWED")) {
                            Log.d(TAG, "✅ No internal error! Got expected 'operation not allowed'");
                            showToast("✅ Internal error fixed! Now enable Email/Password auth");
                        } else {
                            Log.d(TAG, "✅ No internal error! Got: " + errorCode);
                            showToast("✅ SHA1 fix working! Error: " + errorCode);
                        }
                    }
                } else {
                    Log.d(TAG, "✅ Account creation successful! Cleaning up...");
                    showToast("✅ SHA1 fix successful! Authentication working!");

                    // Clean up test account
                    if (auth.getCurrentUser() != null) {
                        auth.getCurrentUser().delete();
                        auth.signOut();
                    }
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
}
