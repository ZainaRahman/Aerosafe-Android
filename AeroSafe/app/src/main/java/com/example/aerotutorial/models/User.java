package com.example.aerotutorial.models;

public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private String location;
    private String role; // "user", "researcher", "admin"
    private long createdAt;
    private boolean active;

    public User() {
        // Required empty constructor for Firebase Realtime Database
        this.active = true;
    }


    public User(String username, String email, String location, String role) {
        this.username = username;
        this.email = email;
        this.location = location;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
        this.active = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
