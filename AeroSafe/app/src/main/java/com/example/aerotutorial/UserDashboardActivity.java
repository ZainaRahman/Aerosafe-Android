package com.example.aerotutorial;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aerotutorial.api.RetrofitClient;
import com.example.aerotutorial.models.AirPollutionResponse;
import com.example.aerotutorial.repository.AuthRepository;
import com.example.aerotutorial.utils.AQICalculator;
import com.example.aerotutorial.utils.ChartHelper;
import com.example.aerotutorial.utils.PredictionEngine;
import com.example.aerotutorial.utils.PreferencesManager;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDashboardActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MaterialToolbar toolbar;
    private TextInputEditText etSearchLocation;
    private MaterialButton btnSearch, btnReportIssue, btnViewAlerts;
    private TextView tvSelectedLocation, tvCurrentAQI, tvAQICategory, tvHealthAlert;
    private TextView tvPredictedAQI, tvTrend, tvPredictionNote;
    private LineChart lineChart;
    private LinearLayout llPreventiveMeasures;
    private ProgressBar progressBar;

    private GoogleMap googleMap;
    private AuthRepository authRepository;
    private PreferencesManager prefsManager;

    private double selectedLat = 23.8103;
    private double selectedLon = 90.4125;
    private String selectedLocation = "Dhaka, Bangladesh";
    private List<Integer> aqiHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        initViews();
        setupToolbar();
        setupMap();
        setupListeners();

        authRepository = new AuthRepository();
        prefsManager = new PreferencesManager(this);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearchLocation = findViewById(R.id.etSearchLocation);
        btnSearch = findViewById(R.id.btnSearch);
        btnReportIssue = findViewById(R.id.btnReportIssue);
        btnViewAlerts = findViewById(R.id.btnViewAlerts);
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        tvCurrentAQI = findViewById(R.id.tvCurrentAQI);
        tvAQICategory = findViewById(R.id.tvAQICategory);
        tvHealthAlert = findViewById(R.id.tvHealthAlert);
        tvPredictedAQI = findViewById(R.id.tvPredictedAQI);
        tvTrend = findViewById(R.id.tvTrend);
        tvPredictionNote = findViewById(R.id.tvPredictionNote);
        lineChart = findViewById(R.id.lineChart);
        llPreventiveMeasures = findViewById(R.id.llPreventiveMeasures);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
            getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> searchLocation());
        btnReportIssue.setOnClickListener(v -> openReportIssue());
        btnViewAlerts.setOnClickListener(v -> viewAlerts());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        LatLng initialPos = new LatLng(selectedLat, selectedLon);
        googleMap.addMarker(new MarkerOptions().position(initialPos).title(selectedLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 10));

        googleMap.setOnMapClickListener(latLng -> {
            selectedLat = latLng.latitude;
            selectedLon = latLng.longitude;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            fetchAQIData();
        });

        fetchAQIData();
    }

    private void searchLocation() {
        String query = etSearchLocation.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Enter a location", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Search: " + query, Toast.LENGTH_SHORT).show();
    }

    private void fetchAQIData() {
        progressBar.setVisibility(View.VISIBLE);

        String apiKey = prefsManager.getApiKey().isEmpty() ? "YOUR_API_KEY" : prefsManager.getApiKey();

        RetrofitClient.getOpenWeatherApi()
            .getAirPollution(selectedLat, selectedLon, apiKey)
            .enqueue(new Callback<AirPollutionResponse>() {
                @Override
                public void onResponse(Call<AirPollutionResponse> call, Response<AirPollutionResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        displayAQIData(response.body());
                    } else {
                        showError("Failed to fetch data");
                    }
                }

                @Override
                public void onFailure(Call<AirPollutionResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    private void displayAQIData(AirPollutionResponse response) {
        if (response.getList() == null || response.getList().isEmpty()) {
            showError("No data available");
            return;
        }

        AirPollutionResponse.AirData data = response.getList().get(0);
        AirPollutionResponse.Components components = data.getComponents();

        int aqi = AQICalculator.calculateOverallAQI(
            components.getPm25(), components.getPm10(), components.getNo2(),
            components.getO3(), components.getSo2(), components.getCo()
        );

        tvCurrentAQI.setText(String.valueOf(aqi));
        String category = AQICalculator.getAQICategory(aqi);
        tvAQICategory.setText(category);

        int aqiColor = AQICalculator.getAQIColor(aqi);
        tvCurrentAQI.setTextColor(aqiColor);
        tvAQICategory.setTextColor(aqiColor);

        String healthAlert = AQICalculator.getHealthAlert(aqi);
        tvHealthAlert.setText(healthAlert);
        tvHealthAlert.setTextColor(aqiColor);

        aqiHistory.add(aqi);
        if (aqiHistory.size() > 7) aqiHistory.remove(0);

        ChartHelper.setupLineChart(lineChart, aqiHistory);

        if (aqiHistory.size() >= 2) {
            PredictionEngine.PredictionResult result = PredictionEngine.predictNextDay(aqiHistory);
            tvPredictedAQI.setText(String.valueOf((int) Math.round(result.predicted)));
            tvTrend.setText(result.getTrend());
            tvPredictionNote.setVisibility(View.VISIBLE);
        } else {
            tvPredictedAQI.setText("--");
            tvPredictionNote.setText("Need more data points");
        }

        updatePreventiveMeasures(aqi);
    }

    private void updatePreventiveMeasures(int aqi) {
        llPreventiveMeasures.removeAllViews();
        for (String measure : AQICalculator.getPreventiveMeasures(aqi)) {
            TextView tv = new TextView(this);
            tv.setText(measure);
            tv.setTextSize(14);
            tv.setPadding(0, 8, 0, 8);
            llPreventiveMeasures.addView(tv);
        }
    }

    private void openReportIssue() {
        startActivity(new Intent(this, ReportIssueActivity.class));
    }

    private void viewAlerts() {
        Toast.makeText(this, "View Alerts - Coming Soon", Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvCurrentAQI.setText("--");
        tvAQICategory.setText("No data");
        tvHealthAlert.setText(message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        authRepository.signOut();
        prefsManager.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
