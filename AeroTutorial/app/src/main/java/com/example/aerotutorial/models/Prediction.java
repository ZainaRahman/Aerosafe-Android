package com.example.aerotutorial.models;

public class Prediction {
    private String id;
    private String location;
    private int predictedAqi;
    private int hoursAhead;
    private String userId;
    private String model;
    private double confidence;
    private long predictionTimestamp;

    // Required empty constructor for Firebase Realtime Database
    public Prediction() {
        this.predictionTimestamp = System.currentTimeMillis();
    }

    public Prediction(String location, int predictedAqi, int hoursAhead) {
        this.location = location;
        this.predictedAqi = predictedAqi;
        this.hoursAhead = hoursAhead;
        this.predictionTimestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPredictedAqi() {
        return predictedAqi;
    }

    public void setPredictedAqi(int predictedAqi) {
        this.predictedAqi = predictedAqi;
    }

    public int getHoursAhead() {
        return hoursAhead;
    }

    public void setHoursAhead(int hoursAhead) {
        this.hoursAhead = hoursAhead;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public long getPredictionTimestamp() {
        return predictionTimestamp;
    }

    public void setPredictionTimestamp(long predictionTimestamp) {
        this.predictionTimestamp = predictionTimestamp;
    }
}

