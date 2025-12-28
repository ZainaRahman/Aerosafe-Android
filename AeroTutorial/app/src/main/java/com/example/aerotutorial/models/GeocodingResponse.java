package com.example.aerotutorial.models;

import com.google.gson.annotations.SerializedName;

public class GeocodingResponse {
    @SerializedName("name")
    private String name;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    @SerializedName("country")
    private String country;

    @SerializedName("state")
    private String state;

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getDisplayName() {
        StringBuilder display = new StringBuilder(name);
        if (state != null && !state.isEmpty()) {
            display.append(", ").append(state);
        }
        if (country != null && !country.isEmpty()) {
            display.append(", ").append(country);
        }
        return display.toString();
    }
}

