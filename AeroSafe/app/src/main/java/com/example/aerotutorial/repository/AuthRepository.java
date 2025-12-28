package com.example.aerotutorial.repository;

import com.example.aerotutorial.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    // Callback interfaces
    public interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    // Sign up with callback
    public void signUp(String email, String password, String username, String role, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser firebaseUser = authResult.getUser();
                if (firebaseUser != null) {
                    // Create user profile
                    User user = new User(username, email, "", role);
                    user.setId(firebaseUser.getUid());

                    createUserProfile(user)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                } else {
                    callback.onFailure("Failed to create user");
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Sign in with callback
    public void signIn(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser firebaseUser = authResult.getUser();
                if (firebaseUser != null) {
                    // Fetch user profile
                    getUserProfile(firebaseUser.getUid())
                        .addOnSuccessListener(dataSnapshot -> {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                user.setId(firebaseUser.getUid());
                                callback.onSuccess(user);
                            } else {
                                callback.onFailure("User profile not found");
                            }
                        })
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                } else {
                    callback.onFailure("Login failed");
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Reset password with callback
    public void resetPassword(String email, OnCompleteListener<Void> listener) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(listener);
    }

    // Legacy methods for backward compatibility
    public Task<AuthResult> signUp(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> signIn(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        return firebaseAuth.sendPasswordResetEmail(email);
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public Task<Void> createUserProfile(User user) {
        String userId = user.getId();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("location", user.getLocation());
        userMap.put("role", user.getRole());
        userMap.put("createdAt", System.currentTimeMillis());
        userMap.put("active", true);

        return databaseReference.child("users").child(userId).setValue(userMap);
    }

    public Task<com.google.firebase.database.DataSnapshot> getUserProfile(String userId) {
        return databaseReference.child("users").child(userId).get();
    }

    public Task<Void> updateUserProfile(String userId, Map<String, Object> updates) {
        return databaseReference.child("users").child(userId).updateChildren(updates);
    }

    public DatabaseReference getAllUsers() {
        return databaseReference.child("users");
    }

    public Query getUsersByRole(String role) {
        return databaseReference.child("users").orderByChild("role").equalTo(role);
    }
}
