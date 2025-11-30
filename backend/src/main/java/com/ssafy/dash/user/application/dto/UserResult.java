package com.ssafy.dash.user.application.dto;

import java.time.LocalDateTime;

public class UserResult {

    private final Long id;
    private final String username;
    private final String email;
    private final LocalDateTime createdAt;
    private final String provider;
    private final String providerId;
    private final String avatarUrl;

    public UserResult(Long id, String username, String email, LocalDateTime createdAt,
            String provider, String providerId, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerId = providerId;
        this.avatarUrl = avatarUrl;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
    
}
