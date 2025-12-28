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
import com.example.aerotutorial.models.Alert;
import com.example.aerotutorial.repository.AlertRepository;
import com.example.aerotutorial.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminAlertsFragment extends Fragment {

    private AutoCompleteTextView actvAlertType, actvSeverity;
    private TextInputEditText etAlertLocation, etAlertMessage;
    private MaterialButton btnCreateAlert;
    private RecyclerView rvAlerts;
    private LinearLayout llEmptyState;

    private AlertsAdapter adapter;
    private List<Alert> alertsList;
    private AlertRepository repository;
    private PreferencesManager prefsManager;

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
        // Alert types
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

        // Severity levels
        String[] severityLevels = {"Low", "Medium", "High", "Critical"};
        ArrayAdapter<String> severityAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            severityLevels
        );
        actvSeverity.setAdapter(severityAdapter);
    }

    private void setupRecyclerView() {
        alertsList = new ArrayList<>();
        adapter = new AlertsAdapter(alertsList, this::onDeactivateAlert);

        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAlerts.setAdapter(adapter);
    }

    private void setupListeners() {
        btnCreateAlert.setOnClickListener(v -> createAlert());
    }

    private void createAlert() {
        String alertType = actvAlertType.getText().toString().trim();
        String severity = actvSeverity.getText().toString().trim();
        String location = etAlertLocation.getText().toString().trim();
        String message = etAlertMessage.getText().toString().trim();

        // Validation
        if (alertType.isEmpty() || severity.isEmpty() ||
            location.isEmpty() || message.isEmpty()) {
            Toast.makeText(requireContext(),
                "All fields are required",
                Toast.LENGTH_SHORT).show();
            return;
        }

        String createdBy = prefsManager.getUserName();
        if (createdBy.isEmpty()) {
            createdBy = "Admin";
        }

        Alert alert = new Alert(alertType, severity, location, message, createdBy);

        repository.createAlert(alert)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(),
                    "Alert created successfully",
                    Toast.LENGTH_SHORT).show();
                clearForm();
                loadAlerts();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(),
                    "Failed to create alert: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
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

