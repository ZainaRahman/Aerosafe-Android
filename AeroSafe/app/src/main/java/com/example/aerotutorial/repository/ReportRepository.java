package com.example.aerotutorial.repository;

import com.example.aerotutorial.models.Report;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

public class ReportRepository {

    private final DatabaseReference databaseReference;

    public ReportRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference().child("reports");
    }

    // Submit a new report
    public Task<Void> submitReport(Report report) {
        String reportId = databaseReference.push().getKey();
        if (reportId != null) {
            report.setId(reportId);
            Map<String, Object> reportMap = new HashMap<>();
            reportMap.put("id", report.getId());
            reportMap.put("reporterName", report.getReporterName());
            reportMap.put("reporterId", report.getReporterId());
            reportMap.put("location", report.getLocation());
            reportMap.put("issueType", report.getIssueType());
            reportMap.put("severity", report.getSeverity());
            reportMap.put("aqiValue", report.getAqiValue());
            reportMap.put("description", report.getDescription());
            reportMap.put("contact", report.getContact());
            reportMap.put("status", report.getStatus());
            reportMap.put("submittedDate", report.getSubmittedDate());
            reportMap.put("resolvedBy", report.getResolvedBy());
            reportMap.put("resolvedDate", report.getResolvedDate());

            return databaseReference.child(reportId).setValue(reportMap);
        }
        return null;
    }

    // Get all reports (for admin)
    public DatabaseReference getAllReports() {
        return databaseReference;
    }

    // Get reports by status
    public Query getReportsByStatus(String status) {
        return databaseReference.orderByChild("status").equalTo(status);
    }

    // Get reports by user
    public Query getReportsByReporter(String reporterId) {
        return databaseReference.orderByChild("reporterId").equalTo(reporterId);
    }

    // Update report status
    public Task<Void> updateReportStatus(String reportId, String status, String resolvedBy, String notes) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("resolvedBy", resolvedBy);
        updates.put("resolvedDate", System.currentTimeMillis());
        updates.put("resolutionNotes", notes);

        return databaseReference.child(reportId).updateChildren(updates);
    }

    // Delete report
    public Task<Void> deleteReport(String reportId) {
        return databaseReference.child(reportId).removeValue();
    }
}
