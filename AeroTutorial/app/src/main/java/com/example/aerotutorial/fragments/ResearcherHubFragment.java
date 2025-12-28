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
import com.example.aerotutorial.adapters.ResearchDataAdapter;
import com.example.aerotutorial.models.AirQualityData;
import com.example.aerotutorial.repository.ResearchDataRepository;
import com.example.aerotutorial.utils.CSVExporter;
import com.example.aerotutorial.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ResearcherHubFragment extends Fragment {

    private RecyclerView rvResearchData;
    private LinearLayout llEmptyState;
    private MaterialButton btnExportCSV;

    private ResearchDataAdapter adapter;
    private List<AirQualityData> dataList;
    private ResearchDataRepository repository;
    private PreferencesManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_researcher_hub, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();

        repository = new ResearchDataRepository();
        prefsManager = new PreferencesManager(requireContext());

        loadResearchData();
    }

    private void initViews(View view) {
        rvResearchData = view.findViewById(R.id.rvResearchData);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        btnExportCSV = view.findViewById(R.id.btnExportCSV);
    }

    private void setupRecyclerView() {
        dataList = new ArrayList<>();
        adapter = new ResearchDataAdapter(dataList, this::onDeleteItem);

        rvResearchData.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResearchData.setAdapter(adapter);
    }

    private void setupListeners() {
        btnExportCSV.setOnClickListener(v -> exportToCSV());
    }

    private void loadResearchData() {
        String userId = prefsManager.getUserId();

        repository.getResearchDataByResearcher(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AirQualityData data = dataSnapshot.getValue(AirQualityData.class);
                    if (data != null) {
                        data.setId(dataSnapshot.getKey());
                        dataList.add(data);
                    }
                }
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                    "Failed to load data: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private void onDeleteItem(AirQualityData data) {
        repository.deleteResearchData(data.getId())
            .addOnSuccessListener(aVoid -> {
                dataList.remove(data);
                adapter.notifyDataSetChanged();
                updateEmptyState();
                Toast.makeText(requireContext(), "Data deleted", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(),
                    "Failed to delete: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void exportToCSV() {
        if (dataList.isEmpty()) {
            Toast.makeText(requireContext(), "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        CSVExporter exporter = new CSVExporter(requireContext());
        exporter.exportResearchData(dataList, new CSVExporter.ExportCallback() {
            @Override
            public void onSuccess(String filePath) {
                Toast.makeText(requireContext(),
                    "Exported to: " + filePath,
                    Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(),
                    "Export failed: " + error,
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (dataList.isEmpty()) {
            rvResearchData.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvResearchData.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadResearchData(); // Refresh data when fragment becomes visible
    }
}
