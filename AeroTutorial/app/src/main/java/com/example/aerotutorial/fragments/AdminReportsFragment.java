package com.example.aerotutorial.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aerotutorial.R;
import com.example.aerotutorial.adapters.ReportsAdapter;
import com.example.aerotutorial.models.Report;
import com.example.aerotutorial.repository.ReportRepository;
import com.example.aerotutorial.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminReportsFragment extends Fragment {

    private RecyclerView rvReports;
    private LinearLayout llEmptyState;
    private MaterialButtonToggleGroup toggleGroupStatus;
    private MaterialButton btnAll, btnPending, btnResolved;

    private ReportsAdapter adapter;
    private List<Report> reportsList;
    private ReportRepository repository;
    private PreferencesManager prefsManager;
    private String currentFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();

        repository = new ReportRepository();
        prefsManager = new PreferencesManager(requireContext());

        loadReports();
    }

    private void initViews(View view) {
        rvReports = view.findViewById(R.id.rvReports);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        toggleGroupStatus = view.findViewById(R.id.toggleGroupStatus);
        btnAll = view.findViewById(R.id.btnAll);
        btnPending = view.findViewById(R.id.btnPending);
        btnResolved = view.findViewById(R.id.btnResolved);
    }

    private void setupRecyclerView() {
        reportsList = new ArrayList<>();
        adapter = new ReportsAdapter(reportsList, this::onReportAction);

        rvReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReports.setAdapter(adapter);
    }

    private void setupListeners() {
        toggleGroupStatus.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnAll) {
                    currentFilter = "All";
                } else if (checkedId == R.id.btnPending) {
                    currentFilter = "Pending";
                } else if (checkedId == R.id.btnResolved) {
                    currentFilter = "Resolved";
                }
                loadReports();
            }
        });
    }

    private void loadReports() {
        if (currentFilter.equals("All")) {
            repository.getAllReports().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    reportsList.clear();
                    for (DataSnapshot reportSnapshot : snapshot.getChildren()) {
                        Report report = reportSnapshot.getValue(Report.class);
                        if (report != null) {
                            report.setId(reportSnapshot.getKey());
                            reportsList.add(report);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handleLoadError(error.toException());
                }
            });
        } else {
            repository.getReportsByStatus(currentFilter).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    reportsList.clear();
                    for (DataSnapshot reportSnapshot : snapshot.getChildren()) {
                        Report report = reportSnapshot.getValue(Report.class);
                        if (report != null) {
                            report.setId(reportSnapshot.getKey());
                            reportsList.add(report);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handleLoadError(error.toException());
                }
            });
        }
    }

    private void handleLoadError(Exception e) {
        Toast.makeText(requireContext(),
            "Failed to load reports: " + e.getMessage(),
            Toast.LENGTH_SHORT).show();
        updateEmptyState();
    }

    private void onReportAction(Report report, String action) {
        switch (action) {
            case "view":
                // TODO: Show report details dialog
                showReportDetails(report);
                break;
            case "resolve":
                resolveReport(report);
                break;
            case "delete":
                deleteReport(report);
                break;
        }
    }

    private void showReportDetails(Report report) {
        // TODO: Implement dialog to show full report details
        Toast.makeText(requireContext(), "View report #" + report.getId(),
                      Toast.LENGTH_SHORT).show();
    }

    private void resolveReport(Report report) {
        String adminId = prefsManager.getUserId();
        String adminName = prefsManager.getUserName();

        repository.updateReportStatus(report.getId(), "Resolved", adminId,
                                     "Report resolved by admin")
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "Report marked as resolved",
                              Toast.LENGTH_SHORT).show();
                loadReports();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(),
                    "Failed to resolve: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void deleteReport(Report report) {
        repository.deleteReport(report.getId())
            .addOnSuccessListener(aVoid -> {
                reportsList.remove(report);
                adapter.notifyDataSetChanged();
                updateEmptyState();
                Toast.makeText(requireContext(), "Report deleted", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(),
                    "Failed to delete: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void updateEmptyState() {
        if (reportsList.isEmpty()) {
            rvReports.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvReports.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReports();
    }
}

