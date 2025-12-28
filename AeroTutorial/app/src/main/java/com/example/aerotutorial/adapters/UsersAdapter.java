package com.example.aerotutorial.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aerotutorial.R;
import com.example.aerotutorial.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private final List<User> usersList;

    public UsersAdapter(List<User> usersList) {
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = usersList.get(position);

        // Username
        holder.tvUsername.setText(user.getUsername());

        // Email
        holder.tvEmail.setText(user.getEmail());

        // Location
        String location = user.getLocation();
        if (location != null && !location.isEmpty()) {
            holder.tvLocation.setText("📍 " + location);
        } else {
            holder.tvLocation.setText("📍 Location not set");
        }

        // Role with color
        String role = user.getRole();
        holder.tvRole.setText(formatRole(role));
        holder.tvRole.setBackgroundColor(getRoleColor(role));
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    private String formatRole(String role) {
        if (role == null) return "User";

        switch (role.toLowerCase()) {
            case "user":
                return "User";
            case "researcher":
                return "Researcher";
            case "admin":
                return "Admin";
            default:
                return role;
        }
    }

    private int getRoleColor(String role) {
        if (role == null) return Color.parseColor("#2196F3");

        switch (role.toLowerCase()) {
            case "user":
                return Color.parseColor("#2196F3"); // Blue
            case "researcher":
                return Color.parseColor("#FF9800"); // Orange
            case "admin":
                return Color.parseColor("#F44336"); // Red
            default:
                return Color.GRAY;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, tvLocation, tvRole;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRole = itemView.findViewById(R.id.tvRole);
        }
    }
}

