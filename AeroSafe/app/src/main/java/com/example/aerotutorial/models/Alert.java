package com.example.aerotutorial.models;

public class Alert {
    private String id;
    private String alertType;
    private String severity;
    private String location;
    private String message;
    private String createdBy;
    private long createdDate;
    private String status; // "Active", "Inactive"

    public Alert() {
        // Required empty constructor for Firestore
    }

    public Alert(String alertType, String severity, String location, String message, String createdBy) {
        this.alertType = alertType;
        this.severity = severity;
        this.location = location;
        this.message = message;
        this.createdBy = createdBy;
        this.createdDate = System.currentTimeMillis();
        this.status = "Active";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

