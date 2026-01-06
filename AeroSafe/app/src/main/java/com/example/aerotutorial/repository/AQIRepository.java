package com.example.aerotutorial.repository;

import android.util.Log;

import com.example.aerotutorial.api.RetrofitClient;
import com.example.aerotutorial.models.AirPollutionResponse;
import com.example.aerotutorial.models.AirQualityData;
import com.example.aerotutorial.utils.AQICalculator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AQIRepository {
    private static final String TAG = "AQIRepository";


    private static final String API_KEY = "98e192f418b2437e52cb54df708958f9";


    public interface AQICallback {
        void onSuccess(AirQualityData data);
        void onFailure(String error);
    }


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


    private AirQualityData convertToAQIData(AirPollutionResponse response, double lat, double lon) {

        AirPollutionResponse.AirData airData = response.getList().get(0);
        AirPollutionResponse.Components components = airData.getComponents();

        int calculatedAQI = AQICalculator.calculateOverallAQI(
            components.getPm25(),
            components.getPm10(),
            components.getNo2(),
            components.getO3(),
            components.getSo2(),
            components.getCo()
        );


        AirQualityData aqiData = new AirQualityData(
            "Location",
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


        aqiData.setTimestamp(airData.getDt() * 1000L);

        return aqiData;
    }


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

    public String getApiKey() {
        return API_KEY;
    }


    public boolean isApiKeyConfigured() {
        return !API_KEY.equals("YOUR_OPENWEATHER_API_KEY_HERE") &&
               !API_KEY.isEmpty();
    }
}

