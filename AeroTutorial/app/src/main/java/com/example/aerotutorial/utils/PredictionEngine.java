package com.example.aerotutorial.utils;

import java.util.List;

public class PredictionEngine {

    public static PredictionResult predictNextDay(List<Integer> series) {
        int n = series.size();
        if (n < 2) {
            return new PredictionResult(0, 0, 0, false);
        }

        double sumX = 0, sumY = 0, sumXX = 0, sumXY = 0;
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = series.get(i);
            sumX += x;
            sumY += y;
            sumXX += x * x;
            sumXY += x * y;
        }

        double denom = n * sumXX - sumX * sumX;
        if (Math.abs(denom) < 1e-8) {
            double avg = 0;
            for (int val : series) {
                avg += val;
            }
            avg /= series.size();
            return new PredictionResult(avg, 0, 0, true);
        }

        double slope = (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;

        double pred = intercept + slope * n; // next day
        pred = Math.max(0, Math.min(500, pred)); // clamp 0-500

        return new PredictionResult(pred, slope, intercept, true);
    }

    public static class PredictionResult {
        public final double predicted;
        public final double slope;
        public final double intercept;
        public final boolean isValid;

        public PredictionResult(double predicted, double slope, double intercept, boolean isValid) {
            this.predicted = predicted;
            this.slope = slope;
            this.intercept = intercept;
            this.isValid = isValid;
        }

        public String getTrend() {
            if (Math.abs(slope) < 0.5) {
                return "→ Stable";
            } else if (slope > 0) {
                return "↑ Increasing";
            } else {
                return "↓ Decreasing";
            }
        }
    }
}

