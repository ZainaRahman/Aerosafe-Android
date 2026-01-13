package com.example.aerotutorial;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.aerotutorial.api.RetrofitClient;
import com.example.aerotutorial.models.AirPollutionResponse;
import com.example.aerotutorial.models.GeocodingResponse;
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
import java.util.Calendar;
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
    private NestedScrollView scrollView;

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
        scrollView = findViewById(R.id.scrollView);
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

        // Add enter key support for search
        etSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                searchLocation();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        LatLng initialPos = new LatLng(selectedLat, selectedLon);
        googleMap.addMarker(new MarkerOptions().position(initialPos).title(selectedLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 10));

        // Fix map scrolling issue - disable parent scrolling when touching map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null && mapFragment.getView() != null) {
            mapFragment.getView().setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        // Disable parent scrolling when touching map
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Re-enable parent scrolling
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            });
        }

        googleMap.setOnMapClickListener(latLng -> {
            selectedLat = latLng.latitude;
            selectedLon = latLng.longitude;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));

            // Update location name using reverse geocoding
            updateLocationName(latLng);
            fetchHistoricalAQIData();
        });

        fetchHistoricalAQIData();
    }

    private void searchLocation() {
        String query = etSearchLocation.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Enter a location", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String apiKey = prefsManager.getApiKey();
        if (apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY")) {
            apiKey = "98e192f418b2437e52cb54df708958f9";
        }

        // Use Geocoding API to search for location
        RetrofitClient.getOpenWeatherApi()
            .geocodeLocation(query, 1, apiKey)
            .enqueue(new Callback<List<GeocodingResponse>>() {
                @Override
                public void onResponse(Call<List<GeocodingResponse>> call, Response<List<GeocodingResponse>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        GeocodingResponse location = response.body().get(0);
                        selectedLat = location.getLat();
                        selectedLon = location.getLon();
                        selectedLocation = location.getName() + ", " + location.getCountry();

                        // Update map
                        LatLng newPos = new LatLng(selectedLat, selectedLon);
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions().position(newPos).title(selectedLocation));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPos, 12));

                        // Update UI
                        tvSelectedLocation.setText(selectedLocation);
                        etSearchLocation.setText("");

                        // Fetch new data
                        fetchHistoricalAQIData();

                        Toast.makeText(UserDashboardActivity.this, "Location found: " + selectedLocation, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserDashboardActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<GeocodingResponse>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UserDashboardActivity.this, "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void fetchHistoricalAQIData() {
        progressBar.setVisibility(View.VISIBLE);

        String tempApiKey = prefsManager.getApiKey();
        if (tempApiKey.isEmpty() || tempApiKey.equals("YOUR_API_KEY")) {
            tempApiKey = "98e192f418b2437e52cb54df708958f9";
        }
        final String apiKey = tempApiKey;

        // Calculate timestamps for last 7 days
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis() / 1000;
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        long startTime = calendar.getTimeInMillis() / 1000;

        // First fetch current data
        RetrofitClient.getOpenWeatherApi()
            .getAirPollution(selectedLat, selectedLon, apiKey)
            .enqueue(new Callback<AirPollutionResponse>() {
                @Override
                public void onResponse(Call<AirPollutionResponse> call, Response<AirPollutionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        displayCurrentAQIData(response.body());
                    }
                    // Then fetch historical data
                    fetchHistoricalData(startTime, endTime, apiKey);
                }

                @Override
                public void onFailure(Call<AirPollutionResponse> call, Throwable t) {
                    // Still try to fetch historical data
                    fetchHistoricalData(startTime, endTime, apiKey);
                }
            });
    }

    private void fetchHistoricalData(long startTime, long endTime, String apiKey) {
        RetrofitClient.getOpenWeatherApi()
            .getHistoricalAirPollution(selectedLat, selectedLon, startTime, endTime, apiKey)
            .enqueue(new Callback<AirPollutionResponse>() {
                @Override
                public void onResponse(Call<AirPollutionResponse> call, Response<AirPollutionResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        processHistoricalData(response.body());
                    } else {
                        showError("Failed to fetch historical data");
                    }
                }

                @Override
                public void onFailure(Call<AirPollutionResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    private void displayCurrentAQIData(AirPollutionResponse response) {
        if (response.getList() == null || response.getList().isEmpty()) {
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

        updatePreventiveMeasures(aqi);
    }

    private void processHistoricalData(AirPollutionResponse response) {
        if (response.getList() == null || response.getList().isEmpty()) {
            showError("No historical data available");
            return;
        }

        aqiHistory.clear();
        List<AirPollutionResponse.AirData> dataList = response.getList();

        // Group data by day and calculate average AQI per day
        int dataSize = dataList.size();
        int daysToShow = Math.min(7, dataSize);
        
        if (dataSize >= 7) {
            // If we have enough data, sample evenly across 7 days
            int step = dataSize / 7;
            for (int i = 0; i < 7; i++) {
                int index = Math.min(i * step, dataSize - 1);
                AirPollutionResponse.AirData data = dataList.get(index);
                AirPollutionResponse.Components components = data.getComponents();

                int aqi = AQICalculator.calculateOverallAQI(
                    components.getPm25(), components.getPm10(), components.getNo2(),
                    components.getO3(), components.getSo2(), components.getCo()
                );
                aqiHistory.add(aqi);
            }
        } else {
            // If we have less data, use all available points
            for (AirPollutionResponse.AirData data : dataList) {
                AirPollutionResponse.Components components = data.getComponents();

                int aqi = AQICalculator.calculateOverallAQI(
                    components.getPm25(), components.getPm10(), components.getNo2(),
                    components.getO3(), components.getSo2(), components.getCo()
                );
                aqiHistory.add(aqi);
            }
        }

        // Ensure we have at least some data
        if (aqiHistory.isEmpty() && !dataList.isEmpty()) {
            AirPollutionResponse.AirData data = dataList.get(0);
            AirPollutionResponse.Components components = data.getComponents();
            int aqi = AQICalculator.calculateOverallAQI(
                components.getPm25(), components.getPm10(), components.getNo2(),
                components.getO3(), components.getSo2(), components.getCo()
            );
            aqiHistory.add(aqi);
        }

        // Update chart
        if (!aqiHistory.isEmpty()) {
            ChartHelper.setupLineChart(lineChart, aqiHistory);
            lineChart.setVisibility(View.VISIBLE);
        }

        // Calculate and display predictions
        if (aqiHistory.size() >= 2) {
            PredictionEngine.PredictionResult result = PredictionEngine.predictNextDay(aqiHistory);
            int predictedValue = (int) Math.round(result.predicted);
            tvPredictedAQI.setText(String.valueOf(predictedValue));
            
            // Set prediction color based on predicted AQI
            int predictionColor = AQICalculator.getAQIColor(predictedValue);
            tvPredictedAQI.setTextColor(predictionColor);
            
            tvTrend.setText(result.getTrend());
            tvPredictionNote.setText("Based on " + aqiHistory.size() + " data points");
            tvPredictionNote.setVisibility(View.VISIBLE);
        } else if (aqiHistory.size() == 1) {
            // If only one data point, show it as prediction
            tvPredictedAQI.setText(String.valueOf(aqiHistory.get(0)));
            tvTrend.setText("→ Stable (Insufficient data)");
            tvPredictionNote.setText("Need more historical data for accurate prediction");
            tvPredictionNote.setVisibility(View.VISIBLE);
        } else {
            tvPredictedAQI.setText("--");
            tvTrend.setText("No data");
            tvPredictionNote.setText("Need more data points");
            tvPredictionNote.setVisibility(View.VISIBLE);
        }
    }

    private void updateLocationName(LatLng latLng) {
        String apiKey = prefsManager.getApiKey();
        if (apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY")) {
            apiKey = "98e192f418b2437e52cb54df708958f9";
        }

        RetrofitClient.getOpenWeatherApi()
            .reverseGeocode(latLng.latitude, latLng.longitude, 1, apiKey)
            .enqueue(new Callback<List<GeocodingResponse>>() {
                @Override
                public void onResponse(Call<List<GeocodingResponse>> call, Response<List<GeocodingResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        GeocodingResponse location = response.body().get(0);
                        selectedLocation = location.getName() + ", " + location.getCountry();
                        tvSelectedLocation.setText(selectedLocation);
                    } else {
                        selectedLocation = "Lat: " + String.format("%.4f", latLng.latitude) +
                                         ", Lon: " + String.format("%.4f", latLng.longitude);
                        tvSelectedLocation.setText(selectedLocation);
                    }
                }

                @Override
                public void onFailure(Call<List<GeocodingResponse>> call, Throwable t) {
                    selectedLocation = "Lat: " + String.format("%.4f", latLng.latitude) +
                                     ", Lon: " + String.format("%.4f", latLng.longitude);
                    tvSelectedLocation.setText(selectedLocation);
                }
            });
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
        try {
            android.util.Log.d("UserDashboard", "Starting AlertsActivity");
            Intent intent = new Intent(this, AlertsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("UserDashboard", "Error starting AlertsActivity: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening alerts: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvCurrentAQI.setText("--");
        tvAQICategory.setText("No data");
        tvHealthAlert.setText(message);
        tvPredictedAQI.setText("--");
        tvTrend.setText("Data unavailable");
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
