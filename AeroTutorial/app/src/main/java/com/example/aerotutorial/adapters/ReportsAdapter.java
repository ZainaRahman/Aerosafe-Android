package com.example.aerotutorial.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aerotutorial.R;
import com.example.aerotutorial.models.Report;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {

    private final List<Report> reportsList;
    private final OnReportActionListener actionListener;

    public interface OnReportActionListener {
        void onAction(Report report, String action);
    }

    public ReportsAdapter(List<Report> reportsList, OnReportActionListener actionListener) {
        this.reportsList = reportsList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Report report = reportsList.get(position);

        // Report ID
        holder.tvReportId.setText("#" + (position + 1));

        // Status with color
        holder.tvStatus.setText(report.getStatus());
        holder.tvStatus.setBackgroundColor(getStatusColor(report.getStatus()));

        // Reporter info
        holder.tvReporterName.setText("👤 " + report.getReporterName());

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String date = sdf.format(new Date(report.getSubmittedDate()));
        holder.tvDate.setText("📅 " + date);

        // Location
        holder.tvLocation.setText("📍 Location: " + report.getLocation());

        // Issue type and severity
        holder.tvIssueType.setText(report.getIssueType());
        holder.tvSeverity.setText(getSeverityIcon(report.getSeverity()) + " " + report.getSeverity());

        // Description
        holder.tvDescription.setText(report.getDescription());

        // Action buttons
        holder.btnViewDetails.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onAction(report, "view");
            }
        });

        holder.btnResolve.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onAction(report, "resolve");
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onAction(report, "delete");
            }
        });

        // Hide resolve button if already resolved
        if ("Resolved".equals(report.getStatus())) {
            holder.btnResolve.setVisibility(View.GONE);
        } else {
            holder.btnResolve.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return reportsList.size();
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "Pending":
                return Color.parseColor("#FFC107"); // Warning yellow
            case "In Progress":
                return Color.parseColor("#2196F3"); // Info blue
            case "Resolved":
                return Color.parseColor("#4CAF50"); // Success green
            default:
                return Color.GRAY;
        }
    }

    private String getSeverityIcon(String severity) {
        if (severity.toLowerCase().contains("low")) {
            return "🟢";
        } else if (severity.toLowerCase().contains("medium")) {
            return "🟡";
        } else if (severity.toLowerCase().contains("high")) {
            return "🔴";
        } else if (severity.toLowerCase().contains("critical")) {
            return "🚨";
        }
        return "⚠️";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportId, tvStatus, tvReporterName, tvDate, tvLocation;
        TextView tvIssueType, tvSeverity, tvDescription;
        MaterialButton btnViewDetails, btnResolve, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportId = itemView.findViewById(R.id.tvReportId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvReporterName = itemView.findViewById(R.id.tvReporterName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvIssueType = itemView.findViewById(R.id.tvIssueType);
            tvSeverity = itemView.findViewById(R.id.tvSeverity);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnResolve = itemView.findViewById(R.id.btnResolve);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

