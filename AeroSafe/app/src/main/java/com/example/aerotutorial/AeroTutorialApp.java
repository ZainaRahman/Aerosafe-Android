package com.example.aerotutorial;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class AeroTutorialApp extends Application {
    private static final String TAG = "AeroTutorialApp";

    @Override
    public void onCreate() {
        super.onCreate();

        try {

            FirebaseApp.initializeApp(this);


            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
        }
    }
}

