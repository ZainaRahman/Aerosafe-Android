package com.example.aerotutorial.repository;

import com.example.aerotutorial.models.Alert;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlertRepository {

    private final DatabaseReference databaseRef;
    private final DatabaseReference alertsRef;

    public AlertRepository() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        alertsRef = databaseRef.child("alerts");
    }

    public Task<List<Alert>> getAlerts() {
        TaskCompletionSource<List<Alert>> taskCompletionSource = new TaskCompletionSource<>();

        Query query = alertsRef.orderByChild("timestamp").limitToLast(50);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Alert> alerts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Alert alert = snapshot.getValue(Alert.class);
                        if (alert != null) {
                            alert.setId(snapshot.getKey());
                            alerts.add(alert);
                        }
                    } catch (Exception e) {

                    }
                }
            
                Collections.reverse(alerts);
                taskCompletionSource.setResult(alerts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        return taskCompletionSource.getTask();
    }

    public Task<List<Alert>> getAlertsByLocation(String location) {
        TaskCompletionSource<List<Alert>> taskCompletionSource = new TaskCompletionSource<>();

        Query query = alertsRef.orderByChild("location").equalTo(location).limitToLast(20);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Alert> alerts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Alert alert = snapshot.getValue(Alert.class);
                        if (alert != null) {
                            alert.setId(snapshot.getKey());
                            alerts.add(alert);
                        }
                    } catch (Exception e) {
                        // Skip invalid entries
                    }
                }
                // Sort by timestamp, newest first
                Collections.sort(alerts, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                taskCompletionSource.setResult(alerts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        return taskCompletionSource.getTask();
    }

    public Task<Void> markAsRead(String alertId) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        alertsRef.child(alertId).child("read").setValue(true)
                .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(null))
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    public Task<Void> createAlert(Alert alert) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        try {
            android.util.Log.d("AlertRepository", "Creating alert in Firebase...");

            // Validate alert object
            if (alert == null) {
                android.util.Log.e("AlertRepository", "Alert object is null");
                taskCompletionSource.setException(new Exception("Alert object cannot be null"));
                return taskCompletionSource.getTask();
            }

            // Validate required fields
            if (alert.getTitle() == null || alert.getTitle().isEmpty()) {
                android.util.Log.e("AlertRepository", "Alert title is empty");
                taskCompletionSource.setException(new Exception("Alert title is required"));
                return taskCompletionSource.getTask();
            }

            if (alert.getSeverity() == null || alert.getSeverity().isEmpty()) {
                android.util.Log.e("AlertRepository", "Alert severity is empty");
                taskCompletionSource.setException(new Exception("Alert severity is required"));
                return taskCompletionSource.getTask();
            }

            String alertId = alertsRef.push().getKey();
            if (alertId != null) {
                alert.setId(alertId);

                android.util.Log.d("AlertRepository", "Saving alert with ID: " + alertId);
                android.util.Log.d("AlertRepository", "Alert details - Title: " + alert.getTitle() +
                    ", Severity: " + alert.getSeverity() + ", Location: " + alert.getLocation());

                alertsRef.child(alertId).setValue(alert)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("AlertRepository", "Alert saved successfully");
                        taskCompletionSource.setResult(null);
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("AlertRepository", "Failed to save alert: " + e.getMessage(), e);
                        taskCompletionSource.setException(e);
                    });
            } else {
                android.util.Log.e("AlertRepository", "Failed to generate alert ID");
                taskCompletionSource.setException(new Exception("Failed to generate alert ID"));
            }
        } catch (Exception e) {
            android.util.Log.e("AlertRepository", "Exception in createAlert", e);
            taskCompletionSource.setException(e);
        }

        return taskCompletionSource.getTask();
    }

    public Task<List<Alert>> getUnreadAlerts() {
        TaskCompletionSource<List<Alert>> taskCompletionSource = new TaskCompletionSource<>();

        Query query = alertsRef.orderByChild("read").equalTo(false);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Alert> alerts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Alert alert = snapshot.getValue(Alert.class);
                        if (alert != null) {
                            alert.setId(snapshot.getKey());
                            alerts.add(alert);
                        }
                    } catch (Exception e) {
                        // Skip invalid entries
                    }
                }
                // Sort by timestamp, newest first
                Collections.sort(alerts, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                taskCompletionSource.setResult(alerts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        return taskCompletionSource.getTask();
    }

    public Task<Void> deleteAlert(String alertId) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        alertsRef.child(alertId).removeValue()
                .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(null))
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    public Task<Integer> getUnreadCount() {
        TaskCompletionSource<Integer> taskCompletionSource = new TaskCompletionSource<>();

        Query query = alertsRef.orderByChild("read").equalTo(false);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                taskCompletionSource.setResult((int) dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        return taskCompletionSource.getTask();
    }

    public Task<List<Alert>> getNearbyAlerts(double userLat, double userLon, double radiusKm) {
        TaskCompletionSource<List<Alert>> taskCompletionSource = new TaskCompletionSource<>();

        try {
            // Get all active alerts
            Query query = alertsRef.orderByChild("active").equalTo(true);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        List<Alert> nearbyAlerts = new ArrayList<>();

                        if (!dataSnapshot.exists()) {
                            android.util.Log.d("AlertRepository", "No alerts found in database");
                            taskCompletionSource.setResult(nearbyAlerts);
                            return;
                        }

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                Alert alert = snapshot.getValue(Alert.class);
                                if (alert != null) {
                                    alert.setId(snapshot.getKey());

                                    // Calculate distance
                                    double distance = alert.distanceTo(userLat, userLon);

                                    // Include if within radius or if no location set (global alert)
                                    if (distance <= radiusKm || distance == Double.MAX_VALUE) {
                                        nearbyAlerts.add(alert);
                                    }
                                }
                            } catch (Exception e) {
                                android.util.Log.e("AlertRepository", "Error parsing alert", e);
                            }
                        }

                        // Sort by timestamp, newest first
                        Collections.sort(nearbyAlerts, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                        android.util.Log.d("AlertRepository", "Found " + nearbyAlerts.size() + " nearby alerts");
                        taskCompletionSource.setResult(nearbyAlerts);
                    } catch (Exception e) {
                        android.util.Log.e("AlertRepository", "Error in onDataChange", e);
                        taskCompletionSource.setException(e);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    android.util.Log.e("AlertRepository", "Database error: " + databaseError.getMessage());
                    taskCompletionSource.setException(databaseError.toException());
                }
            });
        } catch (Exception e) {
            android.util.Log.e("AlertRepository", "Exception in getNearbyAlerts: " + e.getMessage(), e);
            taskCompletionSource.setException(e);
        }

        return taskCompletionSource.getTask();
    }


    public Query getActiveAlerts() {
        // Return active alerts (not deactivated)
        return alertsRef.orderByChild("active").equalTo(true);
    }

    public Task<Void> deactivateAlert(String alertId) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        alertsRef.child(alertId).child("active").setValue(false)
                .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(null))
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }
}
