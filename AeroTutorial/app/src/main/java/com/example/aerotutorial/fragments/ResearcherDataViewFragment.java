package com.example.aerotutorial.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aerotutorial.R;
import com.example.aerotutorial.api.RetrofitClient;
import com.example.aerotutorial.models.AirPollutionResponse;
import com.example.aerotutorial.models.AirQualityData;
import com.example.aerotutorial.repository.ResearchDataRepository;
import com.example.aerotutorial.utils.AQICalculator;
import com.example.aerotutorial.utils.PreferencesManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResearcherDataViewFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private TextView tvSelectedLocation, tvPM25, tvPM10, tvNO2, tvO3, tvSO2, tvCO;
    private TextInputEditText etSearchLocation;
    private MaterialButton btnSearch, btnSaveToHub;

    private ResearchDataRepository researchDataRepository;
    private PreferencesManager prefsManager;

    private double selectedLat = 23.8103;
    private double selectedLon = 90.4125;
    private String selectedLocation = "Dhaka, Bangladesh";
    private AirQualityData currentData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_researcher_data_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupMap();
        setupListeners();

        researchDataRepository = new ResearchDataRepository();
        prefsManager = new PreferencesManager(requireContext());
    }

    private void initViews(View view) {
        tvSelectedLocation = view.findViewById(R.id.tvSelectedLocation);
        tvPM25 = view.findViewById(R.id.tvPM25);
        tvPM10 = view.findViewById(R.id.tvPM10);
        tvNO2 = view.findViewById(R.id.tvNO2);
        tvO3 = view.findViewById(R.id.tvO3);
        tvSO2 = view.findViewById(R.id.tvSO2);
        tvCO = view.findViewById(R.id.tvCO);
        etSearchLocation = view.findViewById(R.id.etSearchLocation);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnSaveToHub = view.findViewById(R.id.btnSaveToHub);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
            getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> searchLocation());
        btnSaveToHub.setOnClickListener(v -> saveToResearchHub());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Set initial position
        LatLng initialPos = new LatLng(selectedLat, selectedLon);
        googleMap.addMarker(new MarkerOptions().position(initialPos).title(selectedLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 10));

        // Map click listener
        googleMap.setOnMapClickListener(latLng -> {
            selectedLat = latLng.latitude;
            selectedLon = latLng.longitude;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            fetchPollutantData();
        });

        // Fetch initial data
        fetchPollutantData();
    }

    private void searchLocation() {
        String query = etSearchLocation.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a location", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement geocoding search
        Toast.makeText(requireContext(), "Search: " + query, Toast.LENGTH_SHORT).show();
    }

    private void fetchPollutantData() {
        tvSelectedLocation.setText("Fetching data...");

        String apiKey = prefsManager.getApiKey();
        if (apiKey.isEmpty()) {
            apiKey = "YOUR_API_KEY"; // Fallback
        }

        RetrofitClient.getOpenWeatherApi()
            .getAirPollution(selectedLat, selectedLon, apiKey)
            .enqueue(new Callback<AirPollutionResponse>() {
                @Override
                public void onResponse(Call<AirPollutionResponse> call,
                                     Response<AirPollutionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        displayPollutantData(response.body());
                    } else {
                        showError("Failed to fetch data");
                    }
                }

                @Override
                public void onFailure(Call<AirPollutionResponse> call, Throwable t) {
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    private void displayPollutantData(AirPollutionResponse response) {
        if (response.getList() == null || response.getList().isEmpty()) {
            showError("No data available");
            return;
        }

        AirPollutionResponse.AirData data = response.getList().get(0);
        AirPollutionResponse.Components components = data.getComponents();

        // Update UI
        tvPM25.setText(String.format("%.2f", components.getPm25()));
        tvPM10.setText(String.format("%.2f", components.getPm10()));
        tvNO2.setText(String.format("%.2f", components.getNo2()));
        tvO3.setText(String.format("%.2f", components.getO3()));
        tvSO2.setText(String.format("%.2f", components.getSo2()));
        tvCO.setText(String.format("%.2f", components.getCo()));

        tvSelectedLocation.setText("📍 " + selectedLocation);

        // Calculate AQI
        int aqi = AQICalculator.calculateOverallAQI(
            components.getPm25(),
            components.getPm10(),
            components.getNo2(),
            components.getO3(),
            components.getSo2(),
            components.getCo()
        );

        // Store current data
        currentData = new AirQualityData(
            selectedLocation,
            selectedLat,
            selectedLon,
            aqi,
            components.getPm25(),
            components.getPm10(),
            components.getNo2(),
            components.getO3(),
            components.getSo2(),
            components.getCo()
        );
    }

    private void saveToResearchHub() {
        if (currentData == null) {
            Toast.makeText(requireContext(), "No data to save", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = prefsManager.getUserId();
        researchDataRepository.saveResearchData(currentData, userId)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "Data saved to Research Hub",
                              Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to save: " + e.getMessage(),
                              Toast.LENGTH_SHORT).show();
            });
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
        tvSelectedLocation.setText("Error loading data");
        tvPM25.setText("--");
        tvPM10.setText("--");
        tvNO2.setText("--");
        tvO3.setText("--");
        tvSO2.setText("--");
        tvCO.setText("--");
    }
}

