package com.example.aerotutorial.repository;

import android.util.Log;

import com.example.aerotutorial.models.Prediction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PredictionRepository {
    private static final String TAG = "PredictionRepository";
    private static final String PREDICTIONS_COLLECTION = "predictions";

    private final DatabaseReference databaseReference;
    private final Random random;

    public PredictionRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference().child(PREDICTIONS_COLLECTION);
        this.random = new Random();
    }

    public interface PredictionCallback {
        void onSuccess(Prediction prediction);
        void onFailure(String error);
    }

    public interface PredictionListCallback {
        void onSuccess(List<Prediction> predictions);
        void onFailure(String error);
    }

    // Generate prediction based on current AQI
    public void generatePrediction(String location, int currentAqi, int hoursAhead, String userId, PredictionCallback callback) {
        // Simple prediction algorithm (can be enhanced with ML model)
        int predictedAqi = calculatePrediction(currentAqi, hoursAhead);

        Prediction prediction = new Prediction(location, predictedAqi, hoursAhead);
        prediction.setUserId(userId);
        prediction.setModel("Simple Linear Model");
        prediction.setConfidence(0.75 + (random.nextDouble() * 0.15)); // 75-90% confidence

        // Save to Realtime Database
        String predictionId = databaseReference.push().getKey();
        if (predictionId != null) {
            prediction.setId(predictionId);

            Map<String, Object> predictionMap = new HashMap<>();
            predictionMap.put("id", prediction.getId());
            predictionMap.put("location", prediction.getLocation());
            predictionMap.put("predictedAqi", prediction.getPredictedAqi());
            predictionMap.put("hoursAhead", prediction.getHoursAhead());
            predictionMap.put("userId", prediction.getUserId());
            predictionMap.put("model", prediction.getModel());
            predictionMap.put("confidence", prediction.getConfidence());
            predictionMap.put("predictionTimestamp", System.currentTimeMillis());

            databaseReference.child(predictionId).setValue(predictionMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Prediction saved with ID: " + predictionId);
                    callback.onSuccess(prediction);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving prediction", e);
                    callback.onFailure(e.getMessage());
                });
        } else {
            callback.onFailure("Failed to generate prediction ID");
        }
    }

    // Simple prediction calculation (can be replaced with ML model)
    private int calculatePrediction(int currentAqi, int hoursAhead) {
        // Simple algorithm: slight random variation over time
        double variation = (random.nextDouble() - 0.5) * 20; // -10 to +10
        double timeImpact = (hoursAhead / 24.0) * 5; // slight increase over days

        int predicted = (int) (currentAqi + variation + timeImpact);
        return Math.max(0, Math.min(500, predicted)); // Keep within 0-500 range
    }

    // Generate multiple predictions (hourly forecast)
    public void generateHourlyForecast(String location, int currentAqi, String userId, PredictionListCallback callback) {
        List<Prediction> predictions = new ArrayList<>();

        for (int hour = 1; hour <= 24; hour++) {
            int predictedAqi = calculatePrediction(currentAqi, hour);
            Prediction prediction = new Prediction(location, predictedAqi, hour);
            prediction.setUserId(userId);
            prediction.setModel("Hourly Forecast Model");
            prediction.setConfidence(0.80 - (hour * 0.01)); // Confidence decreases over time
            predictions.add(prediction);
        }

        // Save all predictions to Realtime Database
        savePredictionsBatch(predictions, callback);
    }

    // Generate daily forecast
    public void generateDailyForecast(String location, int currentAqi, String userId, PredictionListCallback callback) {
        List<Prediction> predictions = new ArrayList<>();

        for (int day = 1; day <= 7; day++) {
            int hoursAhead = day * 24;
            int predictedAqi = calculatePrediction(currentAqi, hoursAhead);
            Prediction prediction = new Prediction(location, predictedAqi, hoursAhead);
            prediction.setUserId(userId);
            prediction.setModel("Daily Forecast Model");
            prediction.setConfidence(0.75 - (day * 0.05)); // Confidence decreases over days
            predictions.add(prediction);
        }

        savePredictionsBatch(predictions, callback);
    }

    private void savePredictionsBatch(List<Prediction> predictions, PredictionListCallback callback) {
        if (predictions.isEmpty()) {
            callback.onSuccess(predictions);
            return;
        }

        // Save predictions one by one
        int[] savedCount = {0};
        for (Prediction prediction : predictions) {
            String predictionId = databaseReference.push().getKey();
            if (predictionId != null) {
                prediction.setId(predictionId);

                Map<String, Object> predictionMap = new HashMap<>();
                predictionMap.put("id", prediction.getId());
                predictionMap.put("location", prediction.getLocation());
                predictionMap.put("predictedAqi", prediction.getPredictedAqi());
                predictionMap.put("hoursAhead", prediction.getHoursAhead());
                predictionMap.put("userId", prediction.getUserId());
                predictionMap.put("model", prediction.getModel());
                predictionMap.put("confidence", prediction.getConfidence());
                predictionMap.put("predictionTimestamp", System.currentTimeMillis());

                databaseReference.child(predictionId).setValue(predictionMap)
                    .addOnSuccessListener(aVoid -> {
                        savedCount[0]++;
                        if (savedCount[0] == predictions.size()) {
                            callback.onSuccess(predictions);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving prediction batch", e);
                        callback.onFailure(e.getMessage());
                    });
            }
        }
    }

    // Get user's predictions
    public void getUserPredictions(String userId, int limit, PredictionListCallback callback) {
        databaseReference.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Prediction> predictions = new ArrayList<>();
                    for (DataSnapshot predSnapshot : snapshot.getChildren()) {
                        Prediction prediction = predSnapshot.getValue(Prediction.class);
                        if (prediction != null) {
                            prediction.setId(predSnapshot.getKey());
                            predictions.add(prediction);
                        }
                    }

                    // Sort by timestamp descending and limit
                    predictions.sort((p1, p2) -> Long.compare(p2.getPredictionTimestamp(), p1.getPredictionTimestamp()));
                    if (predictions.size() > limit) {
                        predictions = predictions.subList(0, limit);
                    }

                    callback.onSuccess(predictions);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error fetching user predictions", error.toException());
                    callback.onFailure(error.getMessage());
                }
            });
    }

    // Get predictions by location
    public void getPredictionsByLocation(String location, int limit, PredictionListCallback callback) {
        databaseReference.orderByChild("location").equalTo(location)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Prediction> predictions = new ArrayList<>();
                    for (DataSnapshot predSnapshot : snapshot.getChildren()) {
                        Prediction prediction = predSnapshot.getValue(Prediction.class);
                        if (prediction != null) {
                            prediction.setId(predSnapshot.getKey());
                            predictions.add(prediction);
                        }
                    }

                    // Sort by timestamp descending and limit
                    predictions.sort((p1, p2) -> Long.compare(p2.getPredictionTimestamp(), p1.getPredictionTimestamp()));
                    if (predictions.size() > limit) {
                        predictions = predictions.subList(0, limit);
                    }

                    callback.onSuccess(predictions);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error fetching location predictions", error.toException());
                    callback.onFailure(error.getMessage());
                }
            });
    }
}

