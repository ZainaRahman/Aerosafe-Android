package com.example.aerotutorial.utils;

import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

/**
 * Quick Firebase Configuration Validator
 * Validates Firebase setup without making network calls
 */
public class FirebaseConfigValidator {

    private static final String TAG = "FirebaseConfigValidator";

    public static void validateConfiguration() {
        Log.d(TAG, "🔍 VALIDATING FIREBASE CONFIGURATION");

        try {
            FirebaseApp app = FirebaseApp.getInstance();
            FirebaseOptions options = app.getOptions();

            // Validate required fields
            String projectId = options.getProjectId();
            String apiKey = options.getApiKey();
            String appId = options.getApplicationId();

            Log.d(TAG, "Checking required fields...");

            if (projectId == null || projectId.isEmpty()) {
                Log.e(TAG, "❌ PROJECT ID is missing!");
            } else {
                Log.d(TAG, "✅ Project ID: " + projectId);

                // Validate project ID format
                if (!projectId.matches("^[a-z0-9-]+$")) {
                    Log.e(TAG, "⚠️  Project ID has invalid format");
                } else {
                    Log.d(TAG, "✅ Project ID format is valid");
                }
            }

            if (apiKey == null || apiKey.isEmpty()) {
                Log.e(TAG, "❌ API KEY is missing!");
            } else {
                Log.d(TAG, "✅ API Key present (length: " + apiKey.length() + ")");

                // Validate API key format (should start with AIza)
                if (!apiKey.startsWith("AIza")) {
                    Log.e(TAG, "⚠️  API Key has invalid format (should start with 'AIza')");
                } else {
                    Log.d(TAG, "✅ API Key format is valid");
                }
            }

            if (appId == null || appId.isEmpty()) {
                Log.e(TAG, "❌ APPLICATION ID is missing!");
            } else {
                Log.d(TAG, "✅ Application ID: " + appId);

                // Validate app ID format
                if (!appId.matches("^1:\\d+:android:[a-f0-9]+$")) {
                    Log.e(TAG, "⚠️  Application ID has invalid format");
                } else {
                    Log.d(TAG, "✅ Application ID format is valid");
                }
            }

            // Check database URL
            String databaseUrl = options.getDatabaseUrl();
            if (databaseUrl != null && !databaseUrl.isEmpty()) {
                Log.d(TAG, "✅ Database URL: " + databaseUrl);

                if (!databaseUrl.contains("firebaseio.com")) {
                    Log.e(TAG, "⚠️  Database URL format looks incorrect");
                } else {
                    Log.d(TAG, "✅ Database URL format is valid");
                }
            } else {
                Log.w(TAG, "⚠️  Database URL is not set (might be normal if not using Realtime Database)");
            }

            // Check storage bucket
            String storageBucket = options.getStorageBucket();
            if (storageBucket != null && !storageBucket.isEmpty()) {
                Log.d(TAG, "✅ Storage Bucket: " + storageBucket);
            } else {
                Log.w(TAG, "⚠️  Storage Bucket is not set (might be normal if not using Storage)");
            }

            Log.d(TAG, "🎉 Firebase configuration validation complete!");

        } catch (Exception e) {
            Log.e(TAG, "❌ Firebase configuration validation failed", e);

            if (e.getMessage().contains("not initialized")) {
                Log.e(TAG, "CAUSE: Firebase not initialized properly");
                Log.e(TAG, "SOLUTION: Check if google-services.json is in the correct location");
            } else {
                Log.e(TAG, "CAUSE: " + e.getMessage());
            }
        }
    }

    /**
     * Check if configuration matches expected values for this project
     */
    public static void validateExpectedConfiguration() {
        Log.d(TAG, "🔍 VALIDATING AGAINST EXPECTED VALUES");

        try {
            FirebaseApp app = FirebaseApp.getInstance();
            FirebaseOptions options = app.getOptions();

            String expectedProjectId = "aerosafe-5d610";
            String expectedProjectNumber = "630961771418";

            String actualProjectId = options.getProjectId();

            if (expectedProjectId.equals(actualProjectId)) {
                Log.d(TAG, "✅ Project ID matches expected value");
            } else {
                Log.e(TAG, "❌ Project ID mismatch!");
                Log.e(TAG, "   Expected: " + expectedProjectId);
                Log.e(TAG, "   Actual: " + actualProjectId);
                Log.e(TAG, "   SOLUTION: Redownload google-services.json for correct project");
            }

            String appId = options.getApplicationId();
            if (appId.contains(expectedProjectNumber)) {
                Log.d(TAG, "✅ Application ID contains expected project number");
            } else {
                Log.e(TAG, "❌ Application ID project number mismatch!");
                Log.e(TAG, "   Expected project number: " + expectedProjectNumber);
                Log.e(TAG, "   Actual app ID: " + appId);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Expected configuration validation failed", e);
        }
    }
}
