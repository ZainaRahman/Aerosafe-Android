package com.example.aerotutorial.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aerotutorial.R;
import com.example.aerotutorial.repository.AlertRepository;
import com.example.aerotutorial.repository.AuthRepository;
import com.example.aerotutorial.repository.ReportRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AdminOverviewFragment extends Fragment {

    private TextView tvTotalUsers, tvTotalResearchers, tvTotalReports, tvActiveAlerts;
    private AuthRepository authRepository;
    private ReportRepository reportRepository;
    private AlertRepository alertRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        authRepository = new AuthRepository();
        reportRepository = new ReportRepository();
        alertRepository = new AlertRepository();

        loadStatistics();
    }

    private void initViews(View view) {
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvTotalResearchers = view.findViewById(R.id.tvTotalResearchers);
        tvTotalReports = view.findViewById(R.id.tvTotalReports);
        tvActiveAlerts = view.findViewById(R.id.tvActiveAlerts);
    }

    private void loadStatistics() {
        // Load total users
        authRepository.getUsersByRole("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTotalUsers.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTotalUsers.setText("--");
            }
        });

        // Load total researchers
        authRepository.getUsersByRole("researcher").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTotalResearchers.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTotalResearchers.setText("--");
            }
        });

        // Load total reports
        reportRepository.getAllReports().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTotalReports.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTotalReports.setText("--");
            }
        });

        // Load active alerts
        alertRepository.getActiveAlerts().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvActiveAlerts.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvActiveAlerts.setText("--");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }
}
