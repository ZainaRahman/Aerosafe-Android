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
import com.example.aerotutorial.models.GeocodingResponse;
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

import java.util.Locale;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResearcherDataViewFragment extends Fragment implements OnMapReadyCallback {

    private static final String API_KEY = "98e192f418b2437e52cb54df708958f9";

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
            
            // Fix map scrolling conflict with NestedScrollView
            if (mapFragment.getView() != null) {
                mapFragment.getView().setOnTouchListener((v, event) -> {
                    int action = event.getAction();
                    switch (action) {
                        case android.view.MotionEvent.ACTION_DOWN:
                        case android.view.MotionEvent.ACTION_MOVE:
                            // Disable parent scrolling when touching map
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            break;
                        case android.view.MotionEvent.ACTION_UP:
                        case android.view.MotionEvent.ACTION_CANCEL:
                            // Re-enable parent scrolling
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                    return false;
                });
            }
        }
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> searchLocation());
        btnSaveToHub.setOnClickListener(v -> saveToResearchHub());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);

        // Set initial position
        LatLng initialPos = new LatLng(selectedLat, selectedLon);
        googleMap.addMarker(new MarkerOptions().position(initialPos).title(selectedLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 10));

        // Map click listener
        googleMap.setOnMapClickListener(latLng -> {
            selectedLat = latLng.latitude;
            selectedLon = latLng.longitude;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Getting address..."));
            
            // Get address from coordinates
            updateLocationFromCoordinates(latLng);
            
            // Fetch pollutant data
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

        tvSelectedLocation.setText("Searching...");

        RetrofitClient.getOpenWeatherApi()
            .geocode(query, 1, API_KEY)
            .enqueue(new Callback<List<GeocodingResponse>>() {
                @Override
                public void onResponse(Call<List<com.example.aerotutorial.models.GeocodingResponse>> call,
                                     Response<List<com.example.aerotutorial.models.GeocodingResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        com.example.aerotutorial.models.GeocodingResponse result = response.body().get(0);
                        selectedLat = result.getLat();
                        selectedLon = result.getLon();
                        selectedLocation = result.getDisplayName();

                        // Update map
                        if (googleMap != null) {
                            LatLng newPos = new LatLng(selectedLat, selectedLon);
                            googleMap.clear();
                            googleMap.addMarker(new MarkerOptions().position(newPos).title(selectedLocation));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPos, 10));
                        }

                        // Fetch data for new location
                        fetchPollutantData();
                    } else {
                        Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show();
                        tvSelectedLocation.setText("📍 " + selectedLocation);
                    }
                }

                @Override
                public void onFailure(Call<List<com.example.aerotutorial.models.GeocodingResponse>> call, Throwable t) {
                    Toast.makeText(requireContext(), "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    tvSelectedLocation.setText("📍 " + selectedLocation);
                }
            });
    }

    private void updateLocationFromCoordinates(LatLng latLng) {
        android.util.Log.d("ResearcherDataView", "Getting address for: " + latLng.latitude + ", " + latLng.longitude);
        android.util.Log.d("ResearcherDataView", "Using API Key: " + API_KEY);

        Call<List<com.example.aerotutorial.models.GeocodingResponse>> call =
                RetrofitClient.getOpenWeatherApi().reverseGeocode(
                        latLng.latitude,
                        latLng.longitude,
                        1,
                        API_KEY
                );

        call.enqueue(new Callback<List<com.example.aerotutorial.models.GeocodingResponse>>() {
            @Override
            public void onResponse(Call<List<com.example.aerotutorial.models.GeocodingResponse>> call,
                                   Response<List<com.example.aerotutorial.models.GeocodingResponse>> response) {
                android.util.Log.d("ResearcherDataView", "Response code: " + response.code());
                android.util.Log.d("ResearcherDataView", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    com.example.aerotutorial.models.GeocodingResponse result = response.body().get(0);
                    selectedLocation = result.getDisplayName();
                    android.util.Log.d("ResearcherDataView", "Location name: " + selectedLocation);
                    tvSelectedLocation.setText("📍 " + selectedLocation);
                    
                    // Update marker with actual location name
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(latLng).title(selectedLocation));
                } else {
                    // Fallback to coordinates
                    android.util.Log.e("ResearcherDataView", "Empty or null response body");
                    selectedLocation = String.format(Locale.US, "%.4f, %.4f", latLng.latitude, latLng.longitude);
                    tvSelectedLocation.setText("📍 " + selectedLocation);
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(latLng).title(selectedLocation));
                    Toast.makeText(requireContext(), "Could not get address. Showing coordinates.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<com.example.aerotutorial.models.GeocodingResponse>> call, Throwable t) {
                // Fallback to coordinates on failure
                android.util.Log.e("ResearcherDataView", "Reverse geocoding failed: " + t.getMessage(), t);
                selectedLocation = String.format(Locale.US, "%.4f, %.4f", latLng.latitude, latLng.longitude);
                tvSelectedLocation.setText("📍 " + selectedLocation);
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(latLng).title(selectedLocation));
                Toast.makeText(requireContext(), "Geocoding error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPollutantData() {
        tvSelectedLocation.setText("Fetching data...");

        RetrofitClient.getOpenWeatherApi()
            .getAirPollution(selectedLat, selectedLon, API_KEY)
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


        int aqi = AQICalculator.calculateOverallAQI(
            components.getPm25(),
            components.getPm10(),
            components.getNo2(),
            components.getO3(),
            components.getSo2(),
            components.getCo()
        );


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
            Toast.makeText(requireContext(), "No data to save. Please select a location first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check Firebase Authentication
        com.google.firebase.auth.FirebaseAuth firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        com.google.firebase.auth.FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(requireContext(), "❌ Not authenticated with Firebase!\n\nPlease log out and log back in.", Toast.LENGTH_LONG).show();
            android.util.Log.e("ResearcherDataView", "Firebase user is NULL - not authenticated!");
            return;
        }
        
        String firebaseUid = currentUser.getUid();
        android.util.Log.d("ResearcherDataView", "Firebase UID: " + firebaseUid);
        android.util.Log.d("ResearcherDataView", "Firebase Email: " + currentUser.getEmail());

        String userId = prefsManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            // Use Firebase UID if no userId in preferences
            userId = firebaseUid;
            android.util.Log.w("ResearcherDataView", "No userId in preferences, using Firebase UID: " + firebaseUid);
        }
        
        android.util.Log.d("ResearcherDataView", "Using userId: " + userId);

        Toast.makeText(requireContext(), "Saving data...", Toast.LENGTH_SHORT).show();
        android.util.Log.d("ResearcherDataView", "Saving data for userId: " + userId + ", Location: " + currentData.getLocation());

        researchDataRepository.saveResearchData(currentData, userId)
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("ResearcherDataView", "✅ Data saved successfully to Firebase!");
                Toast.makeText(requireContext(), "✅ Data saved to Research Hub successfully!",
                              Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                String errorMsg = e.getMessage();
                android.util.Log.e("ResearcherDataView", "❌ Save failed: " + errorMsg, e);
                
                if (errorMsg != null && errorMsg.contains("Permission denied")) {
                    Toast.makeText(requireContext(), 
                        "❌ Firebase Permission Denied!\n\n" +
                        "Firebase User: " + (currentUser.getEmail() != null ? currentUser.getEmail() : "Unknown") + "\n" +
                        "UID: " + firebaseUid + "\n\n" +
                        "Rules may need 5 minutes to update after publishing.",
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), 
                        "❌ Failed to save: " + errorMsg,
                        Toast.LENGTH_LONG).show();
                }
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

