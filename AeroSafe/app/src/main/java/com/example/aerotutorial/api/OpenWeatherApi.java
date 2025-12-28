package com.example.aerotutorial.api;

import com.example.aerotutorial.models.AirPollutionResponse;
import com.example.aerotutorial.models.GeocodingResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherApi {

    // Air Pollution API
    @GET("data/2.5/air_pollution")
    Call<AirPollutionResponse> getAirPollution(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey
    );

    // Historical Air Pollution API (last 7 days)
    @GET("data/2.5/air_pollution/history")
    Call<AirPollutionResponse> getHistoricalAirPollution(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("start") long start,
            @Query("end") long end,
            @Query("appid") String apiKey
    );

    // Geocoding API - Get coordinates from city name
    @GET("geo/1.0/direct")
    Call<List<GeocodingResponse>> geocodeLocation(
            @Query("q") String cityName,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );

    // Reverse Geocoding API - Get city name from coordinates
    @GET("geo/1.0/reverse")
    Call<List<GeocodingResponse>> reverseGeocode(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );
}

