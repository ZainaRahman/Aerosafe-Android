package com.example.aerotutorial.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aerotutorial.R;
import com.example.aerotutorial.models.Alert;

import java.util.List;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    private List<Alert> alerts;
    private OnAlertClickListener onAlertClickListener;

    public interface OnAlertClickListener {
        void onAlertClick(Alert alert);
    }

    public AlertsAdapter(List<Alert> alerts, OnAlertClickListener listener) {
        this.alerts = alerts;
        this.onAlertClickListener = listener;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alerts.get(position);
        holder.bind(alert);
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    class AlertViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView tvEmoji, tvTitle, tvMessage, tvTime, tvSeverity;
        private View severityIndicator;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSeverity = itemView.findViewById(R.id.tvSeverity);
            severityIndicator = itemView.findViewById(R.id.severityIndicator);
        }

        public void bind(Alert alert) {
            tvEmoji.setText(alert.getSeverityEmoji());
            tvTitle.setText(alert.getTitle());
            tvMessage.setText(alert.getMessage());
            tvTime.setText(alert.getTimeAgo());
            tvSeverity.setText(alert.getSeverity().toUpperCase());

            // Set severity color
            int severityColor = alert.getSeverityColor();
            tvSeverity.setTextColor(severityColor);
            severityIndicator.setBackgroundColor(severityColor);

            // Set read/unread state
            if (alert.isRead()) {
                cardView.setAlpha(0.7f);
                cardView.setCardElevation(2f);
            } else {
                cardView.setAlpha(1.0f);
                cardView.setCardElevation(4f);
            }

            // Set click listener
            cardView.setOnClickListener(v -> {
                if (onAlertClickListener != null) {
                    onAlertClickListener.onAlertClick(alert);
                }
            });
        }
    }
}
