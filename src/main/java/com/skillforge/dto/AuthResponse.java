package com.skillforge.dto;

public class AuthResponse {
    private String token;
    private String refreshToken;
    private String email;
    private String fullName;
    private String role;
    private Boolean emailVerified;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(String token, String email, String fullName, String role, Boolean emailVerified, String message) {
        this.token = token;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.emailVerified = emailVerified;
        this.message = message;
    }

    public AuthResponse(String token, String refreshToken, String email, String fullName, String role, Boolean emailVerified, String message) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.emailVerified = emailVerified;
        this.message = message;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
