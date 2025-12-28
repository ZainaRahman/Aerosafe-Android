package com.example.aerotutorial.repository;

import com.example.aerotutorial.models.AirQualityData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

public class ResearchDataRepository {

    private final DatabaseReference databaseReference;

    public ResearchDataRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference().child("research_data");
    }

    public Task<Void> saveResearchData(AirQualityData data, String researcherId) {
        String dataId = databaseReference.push().getKey();
        if (dataId != null) {
            data.setId(dataId);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("id", data.getId());
            dataMap.put("location", data.getLocation());
            dataMap.put("latitude", data.getLatitude());
            dataMap.put("longitude", data.getLongitude());
            dataMap.put("aqi", data.getAqi());
            dataMap.put("pm25", data.getPm25());
            dataMap.put("pm10", data.getPm10());
            dataMap.put("no2", data.getNo2());
            dataMap.put("o3", data.getO3());
            dataMap.put("so2", data.getSo2());
            dataMap.put("co", data.getCo());
            dataMap.put("timestamp", data.getTimestamp());
            dataMap.put("researcherId", researcherId);

            return databaseReference.child(dataId).setValue(dataMap);
        }
        return null;
    }

    public Query getResearchDataByResearcher(String researcherId) {
        return databaseReference.orderByChild("researcherId").equalTo(researcherId);
    }

    public DatabaseReference getAllResearchData() {
        return databaseReference;
    }

    public Query getResearchDataByLocation(String location) {
        return databaseReference.orderByChild("location").equalTo(location);
    }

    public Task<Void> deleteResearchData(String dataId) {
        return databaseReference.child(dataId).removeValue();
    }

    public Task<Void> updateResearchData(String dataId, Map<String, Object> updates) {
        return databaseReference.child(dataId).updateChildren(updates);
    }
}
