package com.example.aerotutorial.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    private MaterialButton btnExportCSV, btnRefresh;
    private TextView tvDataCount;

    private ResearchDataAdapter adapter;
    private List<AirQualityData> dataList;
    private ResearchDataRepository repository;
    private PreferencesManager prefsManager;
    private ValueEventListener dataListener;

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
        btnRefresh = view.findViewById(R.id.btnRefresh);
        tvDataCount = view.findViewById(R.id.tvDataCount);
    }

    private void setupRecyclerView() {
        dataList = new ArrayList<>();
        adapter = new ResearchDataAdapter(dataList, this::onDeleteItem);

        rvResearchData.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResearchData.setAdapter(adapter);
    }

    private void setupListeners() {
        btnExportCSV.setOnClickListener(v -> exportToCSV());
        btnRefresh.setOnClickListener(v -> loadResearchData());
    }

    private void loadResearchData() {
        // Check Firebase Authentication
        com.google.firebase.auth.FirebaseAuth firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        com.google.firebase.auth.FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(requireContext(), "❌ Not authenticated! Please log out and log back in.", Toast.LENGTH_LONG).show();
            android.util.Log.e("ResearcherHub", "Firebase user is NULL - not authenticated!");
            tvDataCount.setText("Error: Not authenticated");
            return;
        }
        
        String firebaseUid = currentUser.getUid();
        android.util.Log.d("ResearcherHub", "Firebase UID: " + firebaseUid);
        android.util.Log.d("ResearcherHub", "Firebase Email: " + currentUser.getEmail());
        
        String userId = prefsManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            // Use Firebase UID if no userId in preferences
            userId = firebaseUid;
            android.util.Log.w("ResearcherHub", "No userId in preferences, using Firebase UID: " + firebaseUid);
        }
        
        final String finalUserId = userId; // Make it final for inner class
        android.util.Log.d("ResearcherHub", "Loading data for userId: " + finalUserId);

        tvDataCount.setText("Loading...");

        // Remove old listener if exists
        if (dataListener != null) {
            repository.getResearchDataByResearcher(finalUserId).removeEventListener(dataListener);
        }

        // Create new listener
        dataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                android.util.Log.d("ResearcherHub", "Snapshot exists: " + snapshot.exists() + ", Children count: " + snapshot.getChildrenCount());
                
                if (!snapshot.exists()) {
                    android.util.Log.d("ResearcherHub", "No data found in Firebase for userId: " + finalUserId);
                }
                
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    android.util.Log.d("ResearcherHub", "Data key: " + dataSnapshot.getKey());
                    android.util.Log.d("ResearcherHub", "Data value: " + dataSnapshot.getValue());
                    
                    AirQualityData data = dataSnapshot.getValue(AirQualityData.class);
                    if (data != null) {
                        data.setId(dataSnapshot.getKey());
                        dataList.add(data);
                        android.util.Log.d("ResearcherHub", "Added data: " + data.getLocation() + ", AQI: " + data.getAqi());
                    } else {
                        android.util.Log.e("ResearcherHub", "Failed to parse data at key: " + dataSnapshot.getKey());
                    }
                }
                adapter.notifyDataSetChanged();
                updateEmptyState();
                android.util.Log.d("ResearcherHub", "Total items loaded: " + dataList.size());
                
                Toast.makeText(requireContext(), "Loaded " + dataList.size() + " records", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("ResearcherHub", "Failed to load data: " + error.getMessage(), error.toException());
                String errorMessage = "Failed to load data: " + error.getMessage();
                
                if (error.getMessage().contains("Permission denied")) {
                    errorMessage = "Permission Denied!\n\nPlease update Firebase Database Rules:\n" +
                                 "1. Go to Firebase Console\n" +
                                 "2. Realtime Database → Rules\n" +
                                 "3. Set rules to allow authenticated users";
                }
                
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                tvDataCount.setText("Error loading data");
                updateEmptyState();
            }
        };

        // Attach listener
        repository.getResearchDataByResearcher(finalUserId).addValueEventListener(dataListener);
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
            tvDataCount.setText("0 records");
            btnExportCSV.setEnabled(false);
        } else {
            rvResearchData.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            String countText = dataList.size() + (dataList.size() == 1 ? " record" : " records");
            tvDataCount.setText(countText);
            btnExportCSV.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadResearchData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up listener
        if (dataListener != null && prefsManager != null) {
            String userId = prefsManager.getUserId();
            if (userId != null && !userId.isEmpty()) {
                repository.getResearchDataByResearcher(userId).removeEventListener(dataListener);
            }
        }
    }
}
