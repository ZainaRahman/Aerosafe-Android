package com.example.aerotutorial.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aerotutorial.R;
import com.example.aerotutorial.models.AirQualityData;
import com.example.aerotutorial.utils.AQICalculator;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResearchDataAdapter extends RecyclerView.Adapter<ResearchDataAdapter.ViewHolder> {

    private final List<AirQualityData> dataList;
    private final OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDelete(AirQualityData data);
    }

    public ResearchDataAdapter(List<AirQualityData> dataList, OnDeleteClickListener deleteListener) {
        this.dataList = dataList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_research_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AirQualityData data = dataList.get(position);

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String timestamp = sdf.format(new Date(data.getTimestamp()));
        holder.tvTimestamp.setText(timestamp);

        // Location
        holder.tvLocation.setText("📍 " + data.getLocation());

        // AQI with color
        holder.tvAQI.setText("AQI: " + data.getAqi());
        int aqiColor = AQICalculator.getAQIColor(data.getAqi());
        holder.tvAQI.setBackgroundColor(aqiColor);

        // Pollutant values
        holder.tvPM25.setText(String.format(Locale.getDefault(), "%.2f", data.getPm25()));
        holder.tvPM10.setText(String.format(Locale.getDefault(), "%.2f", data.getPm10()));
        holder.tvNO2.setText(String.format(Locale.getDefault(), "%.2f", data.getNo2()));
        holder.tvO3.setText(String.format(Locale.getDefault(), "%.2f", data.getO3()));
        holder.tvSO2.setText(String.format(Locale.getDefault(), "%.2f", data.getSo2()));
        holder.tvCO.setText(String.format(Locale.getDefault(), "%.2f", data.getCo()));

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp, tvLocation, tvAQI;
        TextView tvPM25, tvPM10, tvNO2, tvO3, tvSO2, tvCO;
        MaterialButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvAQI = itemView.findViewById(R.id.tvAQI);
            tvPM25 = itemView.findViewById(R.id.tvPM25);
            tvPM10 = itemView.findViewById(R.id.tvPM10);
            tvNO2 = itemView.findViewById(R.id.tvNO2);
            tvO3 = itemView.findViewById(R.id.tvO3);
            tvSO2 = itemView.findViewById(R.id.tvSO2);
            tvCO = itemView.findViewById(R.id.tvCO);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

