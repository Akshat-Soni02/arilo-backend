package com.project_x.project_x_backend.dto;

import java.util.UUID;

public class AuthResponse {
    private String token;
    private String type;
    private UUID userId;
    private String email;
    private String name;
    private String profilePictureUrl;

    public AuthResponse() {}
    public AuthResponse(String token, String type, UUID userId, String email, String name, String profilePictureUrl) {
        this.token = token;
        this.type = type;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    
}
