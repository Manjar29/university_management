package com.university.management.dto;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long expiresInMs;
    private String email;
    private String role;

    public AuthResponse(String token, Long expiresInMs, String email, String role) {
        this.token = token;
        this.expiresInMs = expiresInMs;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getExpiresInMs() { return expiresInMs; }
    public void setExpiresInMs(Long expiresInMs) { this.expiresInMs = expiresInMs; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
