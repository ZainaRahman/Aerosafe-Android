package com.example.aerotutorial.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "AeroSafePrefs";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_LOCATION = "user_location";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // API Key
    public void saveApiKey(String apiKey) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply();
    }

    public String getApiKey() {
        return prefs.getString(KEY_API_KEY, "");
    }

    // User Info
    public void saveUserInfo(String userId, String role, String name, String email, String location) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_ROLE, role)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_LOCATION, location)
                .apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "user");
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserLocation() {
        return prefs.getString(KEY_USER_LOCATION, "");
    }

    public void clearUserInfo() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_ROLE)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_LOCATION)
                .apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}

