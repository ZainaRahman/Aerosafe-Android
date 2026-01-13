package com.example.aerotutorial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.aerotutorial.adapters.AlertsAdapter;
import com.example.aerotutorial.models.Alert;
import com.example.aerotutorial.repository.AlertRepository;
import com.example.aerotutorial.utils.PreferencesManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AlertsActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final double ALERT_RADIUS_KM = 10.0; // 10 km radius

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private AlertsAdapter adapter;
    private AlertRepository alertRepository;
    private PreferencesManager prefsManager;
    private List<Alert> alerts;
    private FusedLocationProviderClient fusedLocationClient;
    
    private double userLatitude = 23.8103; // Default: Dhaka
    private double userLongitude = 90.4125;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_alerts);

            initViews();
            setupToolbar();
            setupRecyclerView();
            setupSwipeRefresh();

            alertRepository = new AlertRepository();
            prefsManager = new PreferencesManager(this);
            alerts = new ArrayList<>();
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            getUserLocationAndLoadAlerts();
        } catch (Exception e) {
            android.util.Log.e("AlertsActivity", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading alerts: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            recyclerView = findViewById(R.id.recyclerView);
            swipeRefresh = findViewById(R.id.swipeRefresh);
            progressBar = findViewById(R.id.progressBar);
            tvEmptyState = findViewById(R.id.tvEmptyState);

            if (toolbar == null || recyclerView == null || swipeRefresh == null ||
                progressBar == null || tvEmptyState == null) {
                throw new RuntimeException("Failed to initialize views - check layout file");
            }
        } catch (Exception e) {
            android.util.Log.e("AlertsActivity", "Error initializing views: " + e.getMessage(), e);
            throw e;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Air Quality Alerts");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        try {
            if (alerts == null) {
                alerts = new ArrayList<>();
            }
            adapter = new AlertsAdapter(alerts, this::onAlertClick);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            android.util.Log.d("AlertsActivity", "RecyclerView setup complete");
        } catch (Exception e) {
            android.util.Log.e("AlertsActivity", "Error setting up RecyclerView", e);
            Toast.makeText(this, "Error setting up alerts view", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::getUserLocationAndLoadAlerts);
        swipeRefresh.setColorSchemeResources(R.color.primary);
    }

    private void getUserLocationAndLoadAlerts() {
        android.util.Log.d("AlertsActivity", "Starting getUserLocationAndLoadAlerts");

        // Check Firebase connection
        try {
            com.google.firebase.database.FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance();
            if (database == null) {
                android.util.Log.e("AlertsActivity", "Firebase Database instance is null!");
                Toast.makeText(this, "Firebase connection error", Toast.LENGTH_SHORT).show();
                loadAlerts(); // Try anyway
                return;
            }
        } catch (Exception e) {
            android.util.Log.e("AlertsActivity", "Firebase check error: " + e.getMessage(), e);
        }

        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.d("AlertsActivity", "Location permission not granted, requesting...");
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
            // Load alerts with default location
            loadAlerts();
            return;
        }

        // Get current location
        android.util.Log.d("AlertsActivity", "Attempting to get last location");
        try {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                        android.util.Log.d("AlertsActivity", "User location: " +
                            userLatitude + ", " + userLongitude);
                    } else {
                        android.util.Log.d("AlertsActivity", "Location is null, using default location");
                    }
                    loadAlerts();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AlertsActivity", "Failed to get location: " + e.getMessage(), e);
                    Toast.makeText(this, "Using default location", Toast.LENGTH_SHORT).show();
                    loadAlerts(); // Load with default location
                });
        } catch (Exception e) {
            android.util.Log.e("AlertsActivity", "Exception getting location: " + e.getMessage(), e);
            loadAlerts(); // Load with default location
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocationAndLoadAlerts();
            } else {
                Toast.makeText(this, "Location permission denied. Showing all alerts.", 
                    Toast.LENGTH_SHORT).show();
                loadAlerts();
            }
        }
    }

    private void loadAlerts() {
        android.util.Log.d("AlertsActivity", "loadAlerts() called");

        if (progressBar != null && !swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }

        android.util.Log.d("AlertsActivity", "Loading alerts within " + ALERT_RADIUS_KM + 
            " km of " + userLatitude + ", " + userLongitude);

        try {
            // First try to get nearby alerts
            alertRepository.getNearbyAlerts(userLatitude, userLongitude, ALERT_RADIUS_KM)
                .addOnSuccessListener(alertList -> {
                    try {
                        android.util.Log.d("AlertsActivity", "getNearbyAlerts succeeded. Count: " +
                            (alertList != null ? alertList.size() : "null"));

                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        if (swipeRefresh != null) {
                            swipeRefresh.setRefreshing(false);
                        }

                        if (alertList == null || alertList.isEmpty()) {
                            android.util.Log.d("AlertsActivity", "No nearby alerts found, trying to load all alerts");
                            // Try loading all active alerts as fallback
                            loadAllActiveAlerts();
                        } else {
                            android.util.Log.d("AlertsActivity", "Displaying " + alertList.size() + " alerts");
                            if (alerts != null) {
                                alerts.clear();
                                alerts.addAll(alertList);
                            }
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                            if (tvEmptyState != null) {
                                tvEmptyState.setVisibility(View.GONE);
                            }
                            if (recyclerView != null) {
                                recyclerView.setVisibility(View.VISIBLE);
                            }

                            Toast.makeText(this, "Found " + alertList.size() + " alert(s) nearby",
                                Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AlertsActivity", "Error processing alerts: " + e.getMessage(), e);
                        showEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AlertsActivity", "Failed to load nearby alerts: " + e.getMessage(), e);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }

                    // Try loading all alerts as fallback
                    android.util.Log.d("AlertsActivity", "Trying to load all active alerts as fallback");
                    loadAllActiveAlerts();
                });
        } catch (Exception e) {
            android.util.Log.e("AlertsActivity", "Exception in loadAlerts: " + e.getMessage(), e);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
            Toast.makeText(this, "Error loading alerts: " + e.getMessage(), Toast.LENGTH_LONG).show();
            showEmptyState();
        }
    }

    private void loadAllActiveAlerts() {
        android.util.Log.d("AlertsActivity", "Loading all active alerts");

        try {
            alertRepository.getAlerts()
                .addOnSuccessListener(alertList -> {
                    try {
                        android.util.Log.d("AlertsActivity", "getAlerts succeeded. Count: " +
                            (alertList != null ? alertList.size() : "null"));

                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        if (swipeRefresh != null) {
                            swipeRefresh.setRefreshing(false);
                        }

                        if (alertList == null || alertList.isEmpty()) {
                            android.util.Log.d("AlertsActivity", "No alerts found at all");
                            showEmptyState();
                        } else {
                            android.util.Log.d("AlertsActivity", "Displaying " + alertList.size() + " alerts");
                            if (alerts != null) {
                                alerts.clear();
                                alerts.addAll(alertList);
                            }
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                            if (tvEmptyState != null) {
                                tvEmptyState.setVisibility(View.GONE);
                            }
                            if (recyclerView != null) {
                                recyclerView.setVisibility(View.VISIBLE);
                            }

                            Toast.makeText(this, "Found " + alertList.size() + " alert(s)",
                                Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AlertsActivity", "Error processing all alerts: " + e.getMessage(), e);
                        showEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AlertsActivity", "Failed to load all alerts: " + e.getMessage(), e);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                    Toast.makeText(this, "Failed to load alerts. Please check your connection.",
                        Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
        } catch (Exception e) {
            android.util.Log.e("AlertsActivity", "Exception in loadAllActiveAlerts: " + e.getMessage(), e);
            showEmptyState();
        }
    }

    private void onAlertClick(Alert alert) {
        try {
            android.util.Log.d("AlertsActivity", "Alert clicked: " + (alert != null ? alert.getTitle() : "null"));

            if (alert == null) {
                android.util.Log.w("AlertsActivity", "Alert is null");
                return;
            }

            // Mark alert as read
            alert.setRead(true);
            if (alert.getId() != null && !alert.getId().isEmpty()) {
                alertRepository.markAsRead(alert.getId());
            }

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            String title = alert.getTitle() != null ? alert.getTitle() : "Alert";
            Toast.makeText(this, "Alert: " + title, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("AlertsActivity", "Error in onAlertClick", e);
            Toast.makeText(this, "Error displaying alert details", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyState() {
        android.util.Log.d("AlertsActivity", "Showing empty state");
        try {
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("🌟 Great news! No air quality alerts within " +
                    (int)ALERT_RADIUS_KM + " km of your location.\n\nWe'll notify you if conditions change.");
            }
        } catch (Exception e) {
            android.util.Log.e("AlertsActivity", "Error showing empty state", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!alerts.isEmpty()) {
            getUserLocationAndLoadAlerts();
        }
    }
}
