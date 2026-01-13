package com.example.aerotutorial.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.aerotutorial.R;
import com.example.aerotutorial.repository.AlertRepository;
import com.example.aerotutorial.repository.AuthRepository;
import com.example.aerotutorial.repository.ReportRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AdminOverviewFragment extends Fragment {

    private TextView tvTotalUsers, tvTotalResearchers, tvTotalReports, tvActiveAlerts;
    private TextView tvTotalAdmins, tvPendingReports, tvTotalAllUsers;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;

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
        tvTotalAdmins = view.findViewById(R.id.tvTotalAdmins);
        tvPendingReports = view.findViewById(R.id.tvPendingReports);
        tvTotalAllUsers = view.findViewById(R.id.tvTotalAllUsers);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::loadStatistics);
            swipeRefresh.setColorSchemeResources(R.color.primary);
        }
    }

    private void loadStatistics() {
        if (progressBar != null && swipeRefresh != null && !swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Load total users (regular users only)
        authRepository.getUsersByRole("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                if (tvTotalUsers != null) {
                    tvTotalUsers.setText(String.valueOf(count));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (tvTotalUsers != null) {
                    tvTotalUsers.setText("--");
                }
            }
        });

        // Load researchers count
        authRepository.getUsersByRole("researcher").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                if (tvTotalResearchers != null) {
                    tvTotalResearchers.setText(String.valueOf(count));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (tvTotalResearchers != null) {
                    tvTotalResearchers.setText("--");
                }
            }
        });

        // Load admins count
        if (tvTotalAdmins != null) {
            authRepository.getUsersByRole("admin").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount();
                    tvTotalAdmins.setText(String.valueOf(count));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    tvTotalAdmins.setText("--");
                }
            });
        }

        // Load total all users
        if (tvTotalAllUsers != null) {
            authRepository.getAllUsers().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount();
                    tvTotalAllUsers.setText(String.valueOf(count));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    tvTotalAllUsers.setText("--");
                }
            });
        }

        // Load total reports
        reportRepository.getAllReports().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalCount = snapshot.getChildrenCount();
                if (tvTotalReports != null) {
                    tvTotalReports.setText(String.valueOf(totalCount));
                }

                // Count pending reports
                if (tvPendingReports != null) {
                    long pendingCount = 0;
                    for (DataSnapshot reportSnapshot : snapshot.getChildren()) {
                        String status = reportSnapshot.child("status").getValue(String.class);
                        if (status == null || "pending".equalsIgnoreCase(status)) {
                            pendingCount++;
                        }
                    }
                    tvPendingReports.setText(String.valueOf(pendingCount));
                }

                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (tvTotalReports != null) {
                    tvTotalReports.setText("--");
                }
                if (tvPendingReports != null) {
                    tvPendingReports.setText("--");
                }
                hideLoading();
            }
        });

        // Load active alerts
        alertRepository.getActiveAlerts().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                if (tvActiveAlerts != null) {
                    tvActiveAlerts.setText(String.valueOf(count));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (tvActiveAlerts != null) {
                    tvActiveAlerts.setText("--");
                }
            }
        });
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }
}
