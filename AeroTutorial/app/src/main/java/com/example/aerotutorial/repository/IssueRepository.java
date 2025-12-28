package com.example.aerotutorial.repository;

import android.util.Log;

import com.example.aerotutorial.models.Issue;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueRepository {
    private static final String TAG = "IssueRepository";
    private static final String ISSUES_COLLECTION = "issues";

    private final DatabaseReference databaseReference;

    public IssueRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference().child(ISSUES_COLLECTION);
    }

    public interface IssueCallback {
        void onSuccess(Issue issue);
        void onFailure(String error);
    }

    public interface IssueListCallback {
        void onSuccess(List<Issue> issues);
        void onFailure(String error);
    }

    // Submit a new issue
    public void submitIssue(Issue issue, IssueCallback callback) {
        String issueId = databaseReference.push().getKey();
        if (issueId != null) {
            issue.setId(issueId);
            issue.setCreatedAt(System.currentTimeMillis());
            issue.setUpdatedAt(System.currentTimeMillis());

            Map<String, Object> issueMap = new HashMap<>();
            issueMap.put("id", issue.getId());
            issueMap.put("userId", issue.getUserId());
            issueMap.put("userName", issue.getUserName());
            issueMap.put("issueType", issue.getIssueType());
            issueMap.put("description", issue.getDescription());
            issueMap.put("location", issue.getLocation());
            issueMap.put("latitude", issue.getLatitude());
            issueMap.put("longitude", issue.getLongitude());
            issueMap.put("status", issue.getStatus());
            issueMap.put("priority", issue.getPriority());
            issueMap.put("imageUrl", issue.getImageUrl());
            issueMap.put("adminResponse", issue.getAdminResponse());
            issueMap.put("createdAt", issue.getCreatedAt());
            issueMap.put("updatedAt", issue.getUpdatedAt());

            databaseReference.child(issueId).setValue(issueMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Issue submitted with ID: " + issueId);
                    callback.onSuccess(issue);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error submitting issue", e);
                    callback.onFailure(e.getMessage());
                });
        } else {
            callback.onFailure("Failed to generate issue ID");
        }
    }

    // Get user's issues
    public void getUserIssues(String userId, IssueListCallback callback) {
        Query query = databaseReference.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Issue> issues = new ArrayList<>();
                for (DataSnapshot issueSnapshot : snapshot.getChildren()) {
                    Issue issue = issueSnapshot.getValue(Issue.class);
                    if (issue != null) {
                        issue.setId(issueSnapshot.getKey());
                        issues.add(issue);
                    }
                }
                // Sort by createdAt descending
                issues.sort((i1, i2) -> Long.compare(i2.getCreatedAt(), i1.getCreatedAt()));
                callback.onSuccess(issues);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error fetching user issues", error.toException());
                callback.onFailure(error.getMessage());
            }
        });
    }

    // Get all issues (for admin/researcher)
    public void getAllIssues(IssueListCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Issue> issues = new ArrayList<>();
                for (DataSnapshot issueSnapshot : snapshot.getChildren()) {
                    Issue issue = issueSnapshot.getValue(Issue.class);
                    if (issue != null) {
                        issue.setId(issueSnapshot.getKey());
                        issues.add(issue);
                    }
                }
                // Sort by createdAt descending
                issues.sort((i1, i2) -> Long.compare(i2.getCreatedAt(), i1.getCreatedAt()));
                callback.onSuccess(issues);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error fetching all issues", error.toException());
                callback.onFailure(error.getMessage());
            }
        });
    }

    // Get issues by status
    public void getIssuesByStatus(String status, IssueListCallback callback) {
        Query query = databaseReference.orderByChild("status").equalTo(status);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Issue> issues = new ArrayList<>();
                for (DataSnapshot issueSnapshot : snapshot.getChildren()) {
                    Issue issue = issueSnapshot.getValue(Issue.class);
                    if (issue != null) {
                        issue.setId(issueSnapshot.getKey());
                        issues.add(issue);
                    }
                }
                // Sort by createdAt descending
                issues.sort((i1, i2) -> Long.compare(i2.getCreatedAt(), i1.getCreatedAt()));
                callback.onSuccess(issues);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error fetching issues by status", error.toException());
                callback.onFailure(error.getMessage());
            }
        });
    }

    // Update issue status
    public void updateIssueStatus(String issueId, String newStatus, IssueCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("updatedAt", System.currentTimeMillis());

        databaseReference.child(issueId).updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Issue status updated");
                getIssue(issueId, callback);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating issue status", e);
                callback.onFailure(e.getMessage());
            });
    }

    // Add admin response
    public void addAdminResponse(String issueId, String response, IssueCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("adminResponse", response);
        updates.put("updatedAt", System.currentTimeMillis());

        databaseReference.child(issueId).updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Admin response added");
                getIssue(issueId, callback);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error adding admin response", e);
                callback.onFailure(e.getMessage());
            });
    }

    // Get single issue
    public void getIssue(String issueId, IssueCallback callback) {
        databaseReference.child(issueId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Issue issue = snapshot.getValue(Issue.class);
                if (issue != null) {
                    issue.setId(snapshot.getKey());
                    callback.onSuccess(issue);
                } else {
                    callback.onFailure("Issue not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error fetching issue", error.toException());
                callback.onFailure(error.getMessage());
            }
        });
    }

    // Delete issue (admin only)
    public void deleteIssue(String issueId, IssueCallback callback) {
        databaseReference.child(issueId).removeValue()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Issue deleted");
                callback.onSuccess(null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting issue", e);
                callback.onFailure(e.getMessage());
            });
    }

    // Get database reference (for real-time listeners if needed)
    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    // Get issues query by user (for real-time updates)
    public Query getUserIssuesQuery(String userId) {
        return databaseReference.orderByChild("userId").equalTo(userId);
    }

    // Get all issues query (for real-time updates)
    public DatabaseReference getAllIssuesQuery() {
        return databaseReference;
    }
}

