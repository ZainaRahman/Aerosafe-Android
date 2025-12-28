package com.example.aerotutorial;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aerotutorial.models.Report;
import com.example.aerotutorial.repository.ReportRepository;
import com.example.aerotutorial.utils.PreferencesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ReportIssueActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etReporterName, etLocation, etAQIValue, etDescription, etContact;
    private AutoCompleteTextView actvIssueType, actvSeverity;
    private MaterialButton btnSubmit, btnCancel;
    private TextView tvStatus;
    private ProgressBar progressBar;

    private ReportRepository repository;
    private PreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        initViews();
        setupToolbar();
        setupDropdowns();
        setupListeners();

        repository = new ReportRepository();
        prefsManager = new PreferencesManager(this);

        // Pre-fill reporter name if available
        String userName = prefsManager.getUserName();
        if (!userName.isEmpty()) {
            etReporterName.setText(userName);
        }

        // Pre-fill location if available
        String userLocation = prefsManager.getUserLocation();
        if (!userLocation.isEmpty()) {
            etLocation.setText(userLocation);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etReporterName = findViewById(R.id.etReporterName);
        etLocation = findViewById(R.id.etLocation);
        actvIssueType = findViewById(R.id.actvIssueType);
        actvSeverity = findViewById(R.id.actvSeverity);
        etAQIValue = findViewById(R.id.etAQIValue);
        etDescription = findViewById(R.id.etDescription);
        etContact = findViewById(R.id.etContact);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        // Issue types
        String[] issueTypes = {
            "High AQI / Poor Air Quality",
            "Industrial Pollution",
            "Vehicle Emissions",
            "Construction Dust",
            "Burning / Smoke",
            "Chemical Odor",
            "Other Environmental Concern"
        };
        ArrayAdapter<String> issueAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            issueTypes
        );
        actvIssueType.setAdapter(issueAdapter);

        // Severity levels
        String[] severityLevels = {
            "Low - Minor concern",
            "Medium - Noticeable impact",
            "High - Significant health risk",
            "Critical - Immediate action required"
        };
        ArrayAdapter<String> severityAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            severityLevels
        );
        actvSeverity.setAdapter(severityAdapter);
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> submitReport());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void submitReport() {
        // Get values
        String reporterName = etReporterName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String issueType = actvIssueType.getText().toString().trim();
        String severity = actvSeverity.getText().toString().trim();
        String aqiValue = etAQIValue.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String contact = etContact.getText().toString().trim();

        // Validation
        if (reporterName.isEmpty()) {
            etReporterName.setError("Name is required");
            etReporterName.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return;
        }

        if (issueType.isEmpty()) {
            actvIssueType.setError("Please select issue type");
            actvIssueType.requestFocus();
            return;
        }

        if (severity.isEmpty()) {
            actvSeverity.setError("Please select severity");
            actvSeverity.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        // Get reporter ID
        String reporterId = prefsManager.getUserId();

        // Create report object
        Report report = new Report(
            reporterName,
            reporterId,
            location,
            issueType,
            severity,
            aqiValue.isEmpty() ? "Not specified" : aqiValue,
            description,
            contact.isEmpty() ? "Not provided" : contact
        );

        // Submit to Firebase
        repository.submitReport(report)
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("✅ Report submitted successfully! Government officials will review your report.");
                tvStatus.setTextColor(getColor(R.color.status_success));

                // Close activity after 2 seconds
                tvStatus.postDelayed(this::finish, 2000);
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("❌ Failed to submit report: " + e.getMessage());
                tvStatus.setTextColor(getColor(R.color.status_error));
            });
    }
}

