package com.example.aerotutorial.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import com.example.aerotutorial.api.RetrofitClient;
import com.example.aerotutorial.models.AirPollutionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AQI API Error Diagnostic Tool
 * Identifies and fixes "error fetching API" issues
 */
public class AQIErrorDiagnostic {

    private static final String TAG = "AQIErrorDiagnostic";
    private Activity activity;

    public AQIErrorDiagnostic(Activity activity) {
        this.activity = activity;
    }

    public void diagnoseAQIError() {
        Log.d(TAG, "🚨 DIAGNOSING AQI API ERROR");

        checkNetworkConnection();
        checkApiKeyConfiguration();
        testApiKeyValidity();
        checkLocationPermissions();
    }

    private void checkNetworkConnection() {
        Log.d(TAG, "Checking network connectivity...");

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Log.d(TAG, "✅ Network is connected: " + activeNetwork.getTypeName());
        } else {
            Log.e(TAG, "❌ No network connection!");
            showToast("❌ AQI Error: No internet connection!");
        }
    }

    private void checkApiKeyConfiguration() {
        Log.d(TAG, "Checking API key configuration...");

        PreferencesManager prefsManager = new PreferencesManager(activity);
        String storedApiKey = prefsManager.getApiKey();

        // Check AQIRepository API key
        String repositoryApiKey = "98e192f418b2437e52cb54df708958f9"; // From AQIRepository

        Log.d(TAG, "Stored API key: " + (storedApiKey.isEmpty() ? "EMPTY" : "SET (" + storedApiKey.length() + " chars)"));
        Log.d(TAG, "Repository API key: " + repositoryApiKey);

        if (storedApiKey.isEmpty()) {
            Log.e(TAG, "❌ No API key stored in preferences!");
            showToast("❌ AQI Error: API key not configured!");
        } else if (storedApiKey.equals("YOUR_API_KEY") || storedApiKey.equals("YOUR_OPENWEATHER_API_KEY_HERE")) {
            Log.e(TAG, "❌ Placeholder API key detected!");
            showToast("❌ AQI Error: Using placeholder API key!");
        } else {
            Log.d(TAG, "✅ API key is configured");
        }

        // Validate API key format
        if (!storedApiKey.isEmpty() && storedApiKey.length() == 32) {
            Log.d(TAG, "✅ API key has valid OpenWeatherMap format (32 characters)");
        } else if (!storedApiKey.isEmpty()) {
            Log.w(TAG, "⚠️  API key length unusual for OpenWeatherMap: " + storedApiKey.length());
        }
    }

    private void testApiKeyValidity() {
        Log.d(TAG, "Testing API key validity with OpenWeatherMap...");

        PreferencesManager prefsManager = new PreferencesManager(activity);
        String apiKey = prefsManager.getApiKey();

        if (apiKey.isEmpty()) {
            // Use the repository API key as fallback
            apiKey = "98e192f418b2437e52cb54df708958f9";
            Log.d(TAG, "Using repository API key for test");
        }

        // Test with London coordinates (known valid location)
        double testLat = 51.5074;
        double testLon = -0.1278;

        RetrofitClient.getOpenWeatherApi()
            .getAirPollution(testLat, testLon, apiKey)
            .enqueue(new Callback<AirPollutionResponse>() {
                @Override
                public void onResponse(Call<AirPollutionResponse> call, Response<AirPollutionResponse> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ API key is VALID! AQI API working correctly");
                        showToast("✅ AQI API Key is working!");

                        if (response.body() != null && response.body().getList() != null) {
                            Log.d(TAG, "   Got " + response.body().getList().size() + " data points");
                        }
                    } else {
                        Log.e(TAG, "❌ API request failed: " + response.code() + " - " + response.message());
                        analyzeApiError(response.code(), response.message());
                    }
                }

                @Override
                public void onFailure(Call<AirPollutionResponse> call, Throwable t) {
                    Log.e(TAG, "❌ Network request failed", t);
                    showToast("❌ AQI API Error: " + t.getMessage());

                    if (t.getMessage().contains("timeout")) {
                        Log.e(TAG, "CAUSE: Request timeout - slow internet or API issues");
                    } else if (t.getMessage().contains("UnknownHost")) {
                        Log.e(TAG, "CAUSE: Cannot reach OpenWeatherMap servers");
                    } else {
                        Log.e(TAG, "CAUSE: " + t.getClass().getSimpleName() + " - " + t.getMessage());
                    }
                }
            });
    }

    private void analyzeApiError(int responseCode, String message) {
        switch (responseCode) {
            case 401:
                Log.e(TAG, "❌ INVALID API KEY (HTTP 401)");
                showToast("❌ Invalid OpenWeatherMap API Key!");
                break;
            case 403:
                Log.e(TAG, "❌ API KEY FORBIDDEN (HTTP 403) - Check subscription");
                showToast("❌ API Key forbidden - check OpenWeatherMap subscription!");
                break;
            case 429:
                Log.e(TAG, "❌ API RATE LIMIT EXCEEDED (HTTP 429)");
                showToast("❌ API rate limit exceeded - wait and try again!");
                break;
            case 404:
                Log.e(TAG, "❌ API ENDPOINT NOT FOUND (HTTP 404)");
                showToast("❌ API endpoint error - check URL configuration!");
                break;
            case 500:
            case 502:
            case 503:
                Log.e(TAG, "❌ OPENWEATHERMAP SERVER ERROR (HTTP " + responseCode + ")");
                showToast("❌ OpenWeatherMap server error - try again later!");
                break;
            default:
                Log.e(TAG, "❌ UNKNOWN API ERROR (HTTP " + responseCode + "): " + message);
                showToast("❌ API Error " + responseCode + ": " + message);
                break;
        }
    }

    private void checkLocationPermissions() {
        Log.d(TAG, "Checking location permissions...");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int fineLocationPerm = activity.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
            int coarseLocationPerm = activity.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);

            if (fineLocationPerm == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Fine location permission granted");
            } else if (coarseLocationPerm == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Coarse location permission granted");
            } else {
                Log.w(TAG, "⚠️  Location permissions not granted - using default location");
                showToast("⚠️ Grant location permission for accurate AQI data");
            }
        }
    }

    public void provideSolutions() {
        Log.d(TAG, "🔧 AQI ERROR SOLUTIONS:");
        Log.d(TAG, "1. Get free API key from: https://openweathermap.org/api");
        Log.d(TAG, "2. Sign up → API Keys → Generate key → Wait 15 minutes for activation");
        Log.d(TAG, "3. Replace API key in AQIRepository.java");
        Log.d(TAG, "4. Or set API key in app preferences");
        Log.d(TAG, "5. Ensure internet connection is active");
        Log.d(TAG, "6. Grant location permission for automatic location detection");

        showToast("Check logcat for AQI error solutions!");
    }

    private void showToast(String message) {
        if (activity != null) {
            activity.runOnUiThread(() ->
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
            );
        }
    }
}
