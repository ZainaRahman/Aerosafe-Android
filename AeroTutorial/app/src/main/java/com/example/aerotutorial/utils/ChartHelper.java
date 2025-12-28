package com.example.aerotutorial.utils;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ChartHelper {

    public static void setupLineChart(LineChart chart, List<Integer> aqiData) {
        if (aqiData == null || aqiData.isEmpty()) {
            chart.clear();
            return;
        }

        // Create entries for chart
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < aqiData.size(); i++) {
            entries.add(new Entry(i, aqiData.get(i)));
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "AQI History");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#BBDEFB"));
        dataSet.setFillAlpha(50);

        // Create line data
        LineData lineData = new LineData(dataSet);

        // Configure chart
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);

        // Configure X axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(aqiData.size());

        // Configure Y axis (left)
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setAxisMinimum(0f);

        // Disable right axis
        chart.getAxisRight().setEnabled(false);

        // Add AQI zones (optional)
        addAQIZones(leftAxis);

        // Refresh chart
        chart.animateX(1000);
        chart.invalidate();
    }

    private static void addAQIZones(YAxis yAxis) {
        // You can add limit lines for AQI thresholds
        // Good: 0-50, Moderate: 51-100, Unhealthy: 101-150, etc.
    }

    public static void clearChart(LineChart chart) {
        if (chart != null) {
            chart.clear();
            chart.invalidate();
        }
    }
}

