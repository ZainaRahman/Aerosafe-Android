package com.example.aerotutorial.repository;

import com.example.aerotutorial.models.Alert;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

public class AlertRepository {

    private final DatabaseReference databaseReference;

    public AlertRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference().child("alerts");
    }

    public Task<Void> createAlert(Alert alert) {
        String alertId = databaseReference.push().getKey();
        if (alertId != null) {
            alert.setId(alertId);
            Map<String, Object> alertMap = new HashMap<>();
            alertMap.put("id", alert.getId());
            alertMap.put("alertType", alert.getAlertType());
            alertMap.put("severity", alert.getSeverity());
            alertMap.put("location", alert.getLocation());
            alertMap.put("message", alert.getMessage());
            alertMap.put("createdBy", alert.getCreatedBy());
            alertMap.put("createdDate", alert.getCreatedDate());
            alertMap.put("status", alert.getStatus());

            return databaseReference.child(alertId).setValue(alertMap);
        }
        return null;
    }

    public Query getActiveAlerts() {
        return databaseReference.orderByChild("status").equalTo("Active");
    }

    public DatabaseReference getAllAlerts() {
        return databaseReference;
    }

    public Query getAlertsByLocation(String location) {
        return databaseReference.orderByChild("location").equalTo(location);
    }

    public Task<Void> deactivateAlert(String alertId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Inactive");
        return databaseReference.child(alertId).updateChildren(updates);
    }

    public Task<Void> deleteAlert(String alertId) {
        return databaseReference.child(alertId).removeValue();
    }
}
