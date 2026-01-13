package com.example.aerotutorial.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Alert {
    private String id;
    private String title;
    private String message;
    private String severity;
    private long timestamp;
    private boolean isRead;
    private String location;
    private boolean active; // For admin functionality
    private String createdBy; // Who created the alert
    private double latitude;
    private double longitude;

    public Alert() {
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.active = true; // Default to active
    }


    // Constructor for admin created alerts
    public Alert(String title, String severity, String location, String message, String createdBy, double latitude, double longitude) {
        this();
        this.title = title;
        this.severity = severity;
        this.location = location;
        this.message = message;
        this.createdBy = createdBy;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public long getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public String getLocation() { return location; }
    public boolean isActive() { return active; }
    public String getCreatedBy() { return createdBy; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setRead(boolean read) { isRead = read; }
    public void setLocation(String location) { this.location = location; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    // Utility methods
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getTimeAgo() {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // Less than 1 minute
            return "Just now";
        } else if (diff < 3600000) { // Less than 1 hour
            int minutes = (int) (diff / 60000);
            return minutes + " min ago";
        } else if (diff < 86400000) { // Less than 1 day
            int hours = (int) (diff / 3600000);
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else { // More than 1 day
            int days = (int) (diff / 86400000);
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }
    }

    public int getSeverityColor() {
        switch (severity.toLowerCase()) {
            case "critical":
                return android.graphics.Color.parseColor("#D32F2F"); // Red
            case "high":
                return android.graphics.Color.parseColor("#F57C00"); // Orange
            case "medium":
                return android.graphics.Color.parseColor("#FBC02D"); // Yellow
            case "low":
            default:
                return android.graphics.Color.parseColor("#388E3C"); // Green
        }
    }

    public String getSeverityEmoji() {
        switch (severity.toLowerCase()) {
            case "critical":
                return "🚨";
            case "high":
                return "⚠️";
            case "medium":
                return "⚡";
            case "low":
            default:
                return "ℹ️";
        }
    }

    // Calculate distance to another location in kilometers using Haversine formula
    public double distanceTo(double targetLat, double targetLon) {
        if (latitude == 0 && longitude == 0) {
            return Double.MAX_VALUE; // No coordinates set
        }

        final int R = 6371; // Radius of Earth in kilometers

        double latDistance = Math.toRadians(targetLat - latitude);
        double lonDistance = Math.toRadians(targetLon - longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(targetLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in kilometers
    }

    public String getDistanceText(double targetLat, double targetLon) {
        double distance = distanceTo(targetLat, targetLon);
        if (distance == Double.MAX_VALUE) {
            return "";
        }
        if (distance < 1) {
            return String.format("%.0f m away", distance * 1000);
        } else {
            return String.format("%.1f km away", distance);
        }
    }
}
