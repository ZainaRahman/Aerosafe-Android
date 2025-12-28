package com.example.aerotutorial.models;

public class Report {
    private String id;
    private String reporterName;
    private String reporterId;
    private String location;
    private String issueType;
    private String severity;
    private String aqiValue;
    private String description;
    private String contact;
    private String status; // "Pending", "In Progress", "Resolved"
    private long submittedDate;
    private String resolvedBy;
    private long resolvedDate;
    private String resolutionNotes;

    public Report() {
        // Required empty constructor for Firestore
    }

    public Report(String reporterName, String reporterId, String location, String issueType,
                  String severity, String aqiValue, String description, String contact) {
        this.reporterName = reporterName;
        this.reporterId = reporterId;
        this.location = location;
        this.issueType = issueType;
        this.severity = severity;
        this.aqiValue = aqiValue;
        this.description = description;
        this.contact = contact;
        this.status = "Pending";
        this.submittedDate = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getAqiValue() {
        return aqiValue;
    }

    public void setAqiValue(String aqiValue) {
        this.aqiValue = aqiValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(long submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public long getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(long resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}

