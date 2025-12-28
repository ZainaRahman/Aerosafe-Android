package com.example.aerotutorial.utils;

import android.graphics.Color;

public class AQICalculator {

    /**
     * Calculate AQI from PM2.5 concentration (μg/m³)
     */
    public static int calculateAqiFromPM25(double pm25) {
        if (pm25 >= 0 && pm25 < 12.1) {
            return linearInterpolation(pm25, 0, 12.0, 0, 50);
        } else if (pm25 >= 12.1 && pm25 < 35.5) {
            return linearInterpolation(pm25, 12.1, 35.4, 51, 100);
        } else if (pm25 >= 35.5 && pm25 < 55.5) {
            return linearInterpolation(pm25, 35.5, 55.4, 101, 150);
        } else if (pm25 >= 55.5 && pm25 < 150.5) {
            return linearInterpolation(pm25, 55.5, 150.4, 151, 200);
        } else if (pm25 >= 150.5 && pm25 < 250.5) {
            return linearInterpolation(pm25, 150.5, 250.4, 201, 300);
        } else if (pm25 >= 250.5 && pm25 < 350.5) {
            return linearInterpolation(pm25, 250.5, 350.4, 301, 400);
        } else if (pm25 >= 350.5 && pm25 <= 500.4) {
            return linearInterpolation(pm25, 350.5, 500.4, 401, 500);
        }
        return 500;
    }

    /**
     * Calculate AQI from PM10 concentration (μg/m³)
     */
    public static int calculateAqiFromPM10(double pm10) {
        if (pm10 >= 0 && pm10 < 55) {
            return linearInterpolation(pm10, 0, 54, 0, 50);
        } else if (pm10 >= 55 && pm10 < 155) {
            return linearInterpolation(pm10, 55, 154, 51, 100);
        } else if (pm10 >= 155 && pm10 < 255) {
            return linearInterpolation(pm10, 155, 254, 101, 150);
        } else if (pm10 >= 255 && pm10 < 355) {
            return linearInterpolation(pm10, 255, 354, 151, 200);
        } else if (pm10 >= 355 && pm10 < 425) {
            return linearInterpolation(pm10, 355, 424, 201, 300);
        } else if (pm10 >= 425 && pm10 < 505) {
            return linearInterpolation(pm10, 425, 504, 301, 400);
        } else if (pm10 >= 505 && pm10 <= 604) {
            return linearInterpolation(pm10, 505, 604, 401, 500);
        }
        return 500;
    }

    /**
     * Calculate overall AQI from all pollutants
     */
    public static int calculateOverallAQI(double pm25, double pm10, double no2,
                                          double o3, double so2, double co) {
        int aqiPM25 = calculateAqiFromPM25(pm25);
        int aqiPM10 = calculateAqiFromPM10(pm10);

        // Return the maximum AQI value (most restrictive pollutant)
        return Math.max(aqiPM25, aqiPM10);
    }

    private static int linearInterpolation(double value, double lowConc, double highConc,
                                          int lowAqi, int highAqi) {
        return (int) Math.round(((value - lowConc) / (highConc - lowConc)) * (highAqi - lowAqi) + lowAqi);
    }

    /**
     * Get AQI category name
     */
    public static String getAQICategory(int aqi) {
        if (aqi <= 50) return "Good";
        else if (aqi <= 100) return "Moderate";
        else if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        else if (aqi <= 200) return "Unhealthy";
        else if (aqi <= 300) return "Very Unhealthy";
        else return "Hazardous";
    }

    /**
     * Get AQI color
     */
    public static int getAQIColor(int aqi) {
        if (aqi <= 50) return Color.parseColor("#00E400"); // Green
        else if (aqi <= 100) return Color.parseColor("#FFFF00"); // Yellow
        else if (aqi <= 150) return Color.parseColor("#FF7E00"); // Orange
        else if (aqi <= 200) return Color.parseColor("#FF0000"); // Red
        else if (aqi <= 300) return Color.parseColor("#8F3F97"); // Purple
        else return Color.parseColor("#7E0023"); // Maroon
    }

    /**
     * Get health alert message
     */
    public static String getHealthAlert(int aqi) {
        if (aqi <= 50) {
            return "Air quality is good. It's a great day to be active outside! 🌟";
        } else if (aqi <= 100) {
            return "Air quality is acceptable. Unusually sensitive people should consider limiting prolonged outdoor exertion.";
        } else if (aqi <= 150) {
            return "Members of sensitive groups may experience health effects. The general public is less likely to be affected.";
        } else if (aqi <= 200) {
            return "Everyone may begin to experience health effects. Members of sensitive groups may experience more serious health effects.";
        } else if (aqi <= 300) {
            return "Health alert: everyone may experience more serious health effects. Avoid outdoor activities! ⚠️";
        } else {
            return "Health warnings of emergency conditions. The entire population is more likely to be affected. Stay indoors! 🚨";
        }
    }

    /**
     * Get preventive measures based on AQI
     */
    public static String[] getPreventiveMeasures(int aqi) {
        if (aqi <= 50) {
            return new String[]{
                "✓ Perfect conditions for outdoor activities",
                "✓ Good time for exercise and sports",
                "✓ Open windows for fresh air"
            };
        } else if (aqi <= 100) {
            return new String[]{
                "⚠ Unusually sensitive people should limit prolonged outdoor exertion",
                "✓ It's okay to be active outside",
                "✓ Monitor air quality if you have respiratory conditions"
            };
        } else if (aqi <= 150) {
            return new String[]{
                "⚠ Sensitive groups should reduce prolonged outdoor exertion",
                "⚠ Children, elderly, and people with respiratory issues should take precautions",
                "✓ Consider wearing a mask outdoors"
            };
        } else if (aqi <= 200) {
            return new String[]{
                "🚫 Everyone should limit prolonged outdoor exertion",
                "⚠ Sensitive groups should avoid outdoor activities",
                "✓ Wear N95/N99 masks if you must go outside",
                "✓ Keep windows closed and use air purifiers"
            };
        } else if (aqi <= 300) {
            return new String[]{
                "🚫 Everyone should avoid outdoor activities",
                "🚫 Stay indoors with windows closed",
                "✓ Use air purifiers if available",
                "✓ Wear N95/N99 masks if outdoor exposure is unavoidable",
                "⚠ Seek medical attention if experiencing symptoms"
            };
        } else {
            return new String[]{
                "🚨 EMERGENCY: Stay indoors!",
                "🚫 Avoid all outdoor activities",
                "✓ Use HEPA air purifiers",
                "✓ Seal windows and doors",
                "⚠ Seek immediate medical attention if experiencing difficulty breathing",
                "📞 Contact local health authorities"
            };
        }
    }
}

