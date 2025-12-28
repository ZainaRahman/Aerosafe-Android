package com.example.aerotutorial.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aerotutorial.R;
import com.example.aerotutorial.models.Alert;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.ViewHolder> {

    private final List<Alert> alertsList;
    private final OnDeactivateClickListener deactivateListener;

    public interface OnDeactivateClickListener {
        void onDeactivate(Alert alert);
    }

    public AlertsAdapter(List<Alert> alertsList, OnDeactivateClickListener deactivateListener) {
        this.alertsList = alertsList;
        this.deactivateListener = deactivateListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_alert, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Alert alert = alertsList.get(position);

        // Alert type
        holder.tvAlertType.setText("🚨 " + alert.getAlertType());

        // Severity with color
        holder.tvSeverity.setText(alert.getSeverity());
        holder.tvSeverity.setBackgroundColor(getSeverityColor(alert.getSeverity()));

        // Location
        holder.tvLocation.setText("📍 " + alert.getLocation());

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault());
        String date = sdf.format(new Date(alert.getCreatedDate()));
        holder.tvDate.setText("📅 " + date);

        // Message
        holder.tvMessage.setText(alert.getMessage());

        // Created by
        holder.tvCreatedBy.setText("Created by: " + alert.getCreatedBy());

        // Deactivate button
        holder.btnDeactivate.setOnClickListener(v -> {
            if (deactivateListener != null) {
                deactivateListener.onDeactivate(alert);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alertsList.size();
    }

    private int getSeverityColor(String severity) {
        switch (severity.toLowerCase()) {
            case "low":
                return Color.parseColor("#4CAF50"); // Green
            case "medium":
                return Color.parseColor("#FFC107"); // Yellow
            case "high":
                return Color.parseColor("#FF9800"); // Orange
            case "critical":
                return Color.parseColor("#F44336"); // Red
            default:
                return Color.GRAY;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlertType, tvSeverity, tvLocation, tvDate, tvMessage, tvCreatedBy;
        MaterialButton btnDeactivate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlertType = itemView.findViewById(R.id.tvAlertType);
            tvSeverity = itemView.findViewById(R.id.tvSeverity);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvCreatedBy = itemView.findViewById(R.id.tvCreatedBy);
            btnDeactivate = itemView.findViewById(R.id.btnDeactivate);
        }
    }
}

