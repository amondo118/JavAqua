package com.example.javaqua.model;

public class User {

    private String userId;
    private String email;
    private String username;

    public User() {}

    public User(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public User(String userId, String email, String username) {
        this.userId = userId;
        this.email = email;
        this.username = username;
    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
