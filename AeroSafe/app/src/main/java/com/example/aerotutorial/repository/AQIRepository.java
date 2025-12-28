package com.example.aerotutorial.repository;

import android.util.Log;

import com.example.aerotutorial.api.RetrofitClient;
import com.example.aerotutorial.models.AirPollutionResponse;
import com.example.aerotutorial.models.AirQualityData;
import com.example.aerotutorial.utils.AQICalculator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for fetching Air Quality Index (AQI) data
 * Uses OpenWeatherMap Air Pollution API
 */
public class AQIRepository {
    private static final String TAG = "AQIRepository";

    // OpenWeatherMap API Key
    // Get your key from: https://openweathermap.org/api
    // 1. Sign up (free)
    // 2. Go to: https://home.openweathermap.org/api_keys
    // 3. Copy your key
    // 4. Wait 15 minutes for activation
    // 5. Replace the key below with your actual key
    private static final String API_KEY = "98e192f418b2437e52cb54df708958f9";

    /**
     * Callback interface for AQI data retrieval
     */
    public interface AQICallback {
        void onSuccess(AirQualityData data);
        void onFailure(String error);
    }

    /**
     * Fetch AQI data for specific coordinates
     *
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param callback Callback for results
     */
    public void fetchAQIByCoordinates(double latitude, double longitude, AQICallback callback) {
        Log.d(TAG, "Fetching AQI for coordinates: " + latitude + ", " + longitude);

        RetrofitClient.getOpenWeatherApi()
            .getAirPollution(latitude, longitude, API_KEY)
            .enqueue(new Callback<AirPollutionResponse>() {
                @Override
                public void onResponse(Call<AirPollutionResponse> call, Response<AirPollutionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AirPollutionResponse airPollutionResponse = response.body();

                        if (airPollutionResponse.getList() != null &&
                            !airPollutionResponse.getList().isEmpty()) {

                            // Convert API response to AirQualityData
                            AirQualityData aqiData = convertToAQIData(
                                airPollutionResponse,
                                latitude,
                                longitude
                            );

                            Log.d(TAG, "AQI data fetched successfully: AQI = " + aqiData.getAqi());
                            callback.onSuccess(aqiData);
                        } else {
                            Log.e(TAG, "No air quality data available in response");
                            callback.onFailure("No air quality data available");
                        }
                    } else {
                        String errorMsg = "Failed to fetch AQI data: " + response.message();
                        Log.e(TAG, errorMsg);
                        callback.onFailure(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<AirPollutionResponse> call, Throwable t) {
                    String errorMsg = "Network error: " + t.getMessage();
                    Log.e(TAG, errorMsg, t);
                    callback.onFailure(errorMsg);
                }
            });
    }

    /**
     * Convert OpenWeatherMap Air Pollution response to AirQualityData model
     *
     * @param response API response
     * @param lat Latitude
     * @param lon Longitude
     * @return AirQualityData object with all pollutant data
     */
    private AirQualityData convertToAQIData(AirPollutionResponse response, double lat, double lon) {
        // Get first data point from response
        AirPollutionResponse.AirData airData = response.getList().get(0);
        AirPollutionResponse.Components components = airData.getComponents();

        // Calculate overall AQI from all pollutants
        int calculatedAQI = AQICalculator.calculateOverallAQI(
            components.getPm25(),
            components.getPm10(),
            components.getNo2(),
            components.getO3(),
            components.getSo2(),
            components.getCo()
        );

        // Create AirQualityData object with all data
        AirQualityData aqiData = new AirQualityData(
            "Location", // Will be set by caller if needed
            lat,
            lon,
            calculatedAQI,
            components.getPm25(),
            components.getPm10(),
            components.getNo2(),
            components.getO3(),
            components.getSo2(),
            components.getCo()
        );

        // Set timestamp (convert from seconds to milliseconds)
        aqiData.setTimestamp(airData.getDt() * 1000L);

        return aqiData;
    }

    /**
     * Fetch historical AQI data for specific coordinates
     * Note: OpenWeatherMap requires start and end timestamps for historical data
     *
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param start Start timestamp (Unix time in seconds)
     * @param end End timestamp (Unix time in seconds)
     * @param callback Callback for results
     */
    public void fetchHistoricalAQI(double latitude, double longitude, long start, long end, AQICallback callback) {
        Log.d(TAG, "Fetching historical AQI for coordinates: " + latitude + ", " + longitude);

        RetrofitClient.getOpenWeatherApi()
            .getHistoricalAirPollution(latitude, longitude, start, end, API_KEY)
            .enqueue(new Callback<AirPollutionResponse>() {
                @Override
                public void onResponse(Call<AirPollutionResponse> call, Response<AirPollutionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AirPollutionResponse airPollutionResponse = response.body();

                        if (airPollutionResponse.getList() != null &&
                            !airPollutionResponse.getList().isEmpty()) {

                            // Get most recent data point
                            AirQualityData aqiData = convertToAQIData(
                                airPollutionResponse,
                                latitude,
                                longitude
                            );

                            Log.d(TAG, "Historical AQI data fetched successfully");
                            callback.onSuccess(aqiData);
                        } else {
                            Log.e(TAG, "No historical data available");
                            callback.onFailure("No historical data available");
                        }
                    } else {
                        String errorMsg = "Failed to fetch historical data: " + response.message();
                        Log.e(TAG, errorMsg);
                        callback.onFailure(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<AirPollutionResponse> call, Throwable t) {
                    String errorMsg = "Network error: " + t.getMessage();
                    Log.e(TAG, errorMsg, t);
                    callback.onFailure(errorMsg);
                }
            });
    }

    /**
     * Get API key (for testing purposes)
     * @return Current API key
     */
    public String getApiKey() {
        return API_KEY;
    }

    /**
     * Check if API key is set
     * @return true if API key is not the default placeholder
     */
    public boolean isApiKeyConfigured() {
        return !API_KEY.equals("YOUR_OPENWEATHER_API_KEY_HERE") &&
               !API_KEY.isEmpty();
    }
}

