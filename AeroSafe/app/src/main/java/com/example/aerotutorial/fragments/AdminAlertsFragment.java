package com.example.aerotutorial.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aerotutorial.R;
import com.example.aerotutorial.adapters.AlertsAdapter;
import com.example.aerotutorial.api.RetrofitClient;
import com.example.aerotutorial.models.Alert;
import com.example.aerotutorial.models.GeocodingResponse;
import com.example.aerotutorial.repository.AlertRepository;
import com.example.aerotutorial.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAlertsFragment extends Fragment {

    private static final String API_KEY = "98e192f418b2437e52cb54df708958f9";

    private AutoCompleteTextView actvAlertType, actvSeverity;
    private TextInputEditText etAlertLocation, etAlertMessage;
    private MaterialButton btnCreateAlert;
    private RecyclerView rvAlerts;
    private LinearLayout llEmptyState;

    private AlertsAdapter adapter;
    private List<Alert> alertsList;
    private AlertRepository repository;
    private PreferencesManager prefsManager;
    
    private double alertLatitude = 0;
    private double alertLongitude = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupDropdowns();
        setupRecyclerView();
        setupListeners();

        repository = new AlertRepository();
        prefsManager = new PreferencesManager(requireContext());

        loadAlerts();
    }

    private void initViews(View view) {
        actvAlertType = view.findViewById(R.id.actvAlertType);
        actvSeverity = view.findViewById(R.id.actvSeverity);
        etAlertLocation = view.findViewById(R.id.etAlertLocation);
        etAlertMessage = view.findViewById(R.id.etAlertMessage);
        btnCreateAlert = view.findViewById(R.id.btnCreateAlert);
        rvAlerts = view.findViewById(R.id.rvAlerts);
        llEmptyState = view.findViewById(R.id.llEmptyState);
    }

    private void setupDropdowns() {
        // Alert Types
        String[] alertTypes = {
            "High AQI Alert",
            "Health Advisory",
            "Pollution Warning",
            "Emergency Alert",
            "General Notice"
        };
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            alertTypes
        );
        actvAlertType.setAdapter(typeAdapter);

        // Make alert type dropdown clickable
        actvAlertType.setOnClickListener(v -> {
            actvAlertType.showDropDown();
        });

        // Set default selection
        actvAlertType.setOnItemClickListener((parent, view, position, id) -> {
            // Item selected, do nothing special
        });

        // Severity Levels
        String[] severityLevels = {"Low", "Medium", "High", "Critical"};
        ArrayAdapter<String> severityAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            severityLevels
        );
        actvSeverity.setAdapter(severityAdapter);

        // Make severity dropdown clickable
        actvSeverity.setOnClickListener(v -> {
            actvSeverity.showDropDown();
        });

        // Set default selection
        actvSeverity.setOnItemClickListener((parent, view, position, id) -> {
            // Item selected, do nothing special
        });
    }

    private void setupRecyclerView() {
        alertsList = new ArrayList<>();
        adapter = new AlertsAdapter(alertsList, this::onDeactivateAlert);

        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAlerts.setAdapter(adapter);
    }

    private void setupListeners() {
        btnCreateAlert.setOnClickListener(v -> createAlert());
        
        // Add listener to get coordinates when location is entered
        etAlertLocation.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String location = etAlertLocation.getText().toString().trim();
                if (!location.isEmpty()) {
                    geocodeLocation(location);
                }
            }
        });
    }

    private void geocodeLocation(String location) {
        RetrofitClient.getOpenWeatherApi()
            .geocode(location, 1, API_KEY)
            .enqueue(new Callback<List<GeocodingResponse>>() {
                @Override
                public void onResponse(Call<List<GeocodingResponse>> call,
                                     Response<List<GeocodingResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        GeocodingResponse result = response.body().get(0);
                        alertLatitude = result.getLat();
                        alertLongitude = result.getLon();
                        android.util.Log.d("AdminAlerts", "Geocoded location: " + 
                            alertLatitude + ", " + alertLongitude);
                        Toast.makeText(requireContext(), 
                            "✓ Location found: " + result.getDisplayName(), 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        alertLatitude = 0;
                        alertLongitude = 0;
                        Toast.makeText(requireContext(), 
                            "⚠ Location not found. Alert will be visible to all users.", 
                            Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<GeocodingResponse>> call, Throwable t) {
                    alertLatitude = 0;
                    alertLongitude = 0;
                    android.util.Log.e("AdminAlerts", "Geocoding failed: " + t.getMessage());
                }
            });
    }

    private void createAlert() {
        String alertType = actvAlertType.getText().toString().trim();
        String severity = actvSeverity.getText().toString().trim();
        String location = etAlertLocation.getText().toString().trim();
        String message = etAlertMessage.getText().toString().trim();

        android.util.Log.d("AdminAlerts", "Creating alert - Type: " + alertType + ", Severity: " + severity);

        if (alertType.isEmpty() || severity.isEmpty() ||
            location.isEmpty() || message.isEmpty()) {
            Toast.makeText(requireContext(),
                "All fields are required",
                Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double-clicks
        btnCreateAlert.setEnabled(false);

        String createdBy = prefsManager.getUserName();
        if (createdBy.isEmpty()) {
            createdBy = "Admin";
        }

        android.util.Log.d("AdminAlerts", "Alert created by: " + createdBy);

        // Geocode location if not already done
        if (alertLatitude == 0 && alertLongitude == 0) {
            Toast.makeText(requireContext(), "Getting location coordinates...", Toast.LENGTH_SHORT).show();
            geocodeLocationAndCreateAlert(alertType, severity, location, message, createdBy);
            return;
        }

        try {
            Alert alert = new Alert(alertType, severity, location, message, createdBy, alertLatitude, alertLongitude);

            android.util.Log.d("AdminAlerts", "Alert object created. Saving to Firebase...");
            android.util.Log.d("AdminAlerts", "Location: " + location + " (" + alertLatitude + ", " + alertLongitude + ")");

            repository.createAlert(alert)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("AdminAlerts", "Alert saved successfully to Firebase");
                    btnCreateAlert.setEnabled(true);
                    Toast.makeText(requireContext(),
                        "✅ Alert created successfully",
                        Toast.LENGTH_SHORT).show();
                    clearForm();
                    loadAlerts();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AdminAlerts", "Failed to create alert", e);
                    btnCreateAlert.setEnabled(true);

                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("Permission denied")) {
                        Toast.makeText(requireContext(),
                            "❌ Permission denied. Check Firebase Database Rules.",
                            Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(),
                            "❌ Failed to create alert: " + errorMsg,
                            Toast.LENGTH_LONG).show();
                    }
                });
        } catch (Exception e) {
            android.util.Log.e("AdminAlerts", "Exception creating alert", e);
            btnCreateAlert.setEnabled(true);
            Toast.makeText(requireContext(),
                "Error: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
        }
    }

    private void geocodeLocationAndCreateAlert(String alertType, String severity, 
                                               String location, String message, String createdBy) {
        android.util.Log.d("AdminAlerts", "Geocoding location: " + location);

        RetrofitClient.getOpenWeatherApi()
            .geocode(location, 1, API_KEY)
            .enqueue(new Callback<List<GeocodingResponse>>() {
                @Override
                public void onResponse(Call<List<GeocodingResponse>> call,
                                     Response<List<GeocodingResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        GeocodingResponse result = response.body().get(0);
                        alertLatitude = result.getLat();
                        alertLongitude = result.getLon();
                        android.util.Log.d("AdminAlerts", "Geocoding success: " + alertLatitude + ", " + alertLongitude);
                    } else {
                        android.util.Log.w("AdminAlerts", "Geocoding returned no results");
                    }
                    
                    try {
                        Alert alert = new Alert(alertType, severity, location, message,
                                              createdBy, alertLatitude, alertLongitude);

                        android.util.Log.d("AdminAlerts", "Saving alert to Firebase...");

                        repository.createAlert(alert)
                            .addOnSuccessListener(aVoid -> {
                                android.util.Log.d("AdminAlerts", "Alert created successfully");
                                btnCreateAlert.setEnabled(true);
                                Toast.makeText(requireContext(),
                                    "✅ Alert created successfully",
                                    Toast.LENGTH_SHORT).show();
                                clearForm();
                                loadAlerts();
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("AdminAlerts", "Failed to create alert", e);
                                btnCreateAlert.setEnabled(true);

                                String errorMsg = e.getMessage();
                                if (errorMsg != null && errorMsg.contains("Permission denied")) {
                                    Toast.makeText(requireContext(),
                                        "❌ Permission denied. Check Firebase Database Rules.",
                                        Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(requireContext(),
                                        "❌ Failed to create alert: " + errorMsg,
                                        Toast.LENGTH_LONG).show();
                                }
                            });
                    } catch (Exception e) {
                        android.util.Log.e("AdminAlerts", "Exception creating alert", e);
                        btnCreateAlert.setEnabled(true);
                        Toast.makeText(requireContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<List<GeocodingResponse>> call, Throwable t) {
                    android.util.Log.e("AdminAlerts", "Geocoding failed", t);

                    // Create alert without coordinates
                    try {
                        Alert alert = new Alert(alertType, severity, location, message,
                                              createdBy, 0, 0);

                        android.util.Log.d("AdminAlerts", "Saving alert without coordinates...");

                        repository.createAlert(alert)
                            .addOnSuccessListener(aVoid -> {
                                android.util.Log.d("AdminAlerts", "Alert created without location");
                                btnCreateAlert.setEnabled(true);
                                Toast.makeText(requireContext(),
                                    "✅ Alert created (location not found)",
                                    Toast.LENGTH_SHORT).show();
                                clearForm();
                                loadAlerts();
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("AdminAlerts", "Failed to create alert", e);
                                btnCreateAlert.setEnabled(true);

                                String errorMsg = e.getMessage();
                                if (errorMsg != null && errorMsg.contains("Permission denied")) {
                                    Toast.makeText(requireContext(),
                                        "❌ Permission denied. Check Firebase Database Rules.",
                                        Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(requireContext(),
                                        "❌ Failed to create alert: " + errorMsg,
                                        Toast.LENGTH_LONG).show();
                                }
                            });
                    } catch (Exception e) {
                        android.util.Log.e("AdminAlerts", "Exception creating alert", e);
                        btnCreateAlert.setEnabled(true);
                        Toast.makeText(requireContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void loadAlerts() {
        repository.getActiveAlerts().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alertsList.clear();
                for (DataSnapshot alertSnapshot : snapshot.getChildren()) {
                    Alert alert = alertSnapshot.getValue(Alert.class);
                    if (alert != null) {
                        alert.setId(alertSnapshot.getKey());
                        alertsList.add(alert);
                    }
                }
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                    "Failed to load alerts: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private void onDeactivateAlert(Alert alert) {
        repository.deactivateAlert(alert.getId())
            .addOnSuccessListener(aVoid -> {
                alertsList.remove(alert);
                adapter.notifyDataSetChanged();
                updateEmptyState();
                Toast.makeText(requireContext(),
                    "Alert deactivated",
                    Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(),
                    "Failed to deactivate: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void clearForm() {
        actvAlertType.setText("");
        actvSeverity.setText("");
        etAlertLocation.setText("");
        etAlertMessage.setText("");
        alertLatitude = 0;
        alertLongitude = 0;
    }

    private void updateEmptyState() {
        if (alertsList.isEmpty()) {
            rvAlerts.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvAlerts.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAlerts();
    }
}

