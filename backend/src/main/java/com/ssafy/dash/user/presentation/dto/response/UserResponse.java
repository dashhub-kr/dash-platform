package com.ssafy.dash.user.presentation.dto.response;

import java.time.LocalDateTime;

import com.ssafy.dash.user.application.dto.UserResult;

public class UserResponse {

    private final Long id;
    private final String username;
    private final String email;
    private final LocalDateTime createdAt;
    private final String provider;
    private final String providerId;
    private final String avatarUrl;

    public UserResponse(Long id, String username, String email, LocalDateTime createdAt,
            String provider, String providerId, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerId = providerId;
        this.avatarUrl = avatarUrl;
    }

    public static UserResponse from(UserResult result) {
        return new UserResponse(result.getId(), result.getUsername(), result.getEmail(),
                result.getCreatedAt(), result.getProvider(), result.getProviderId(), result.getAvatarUrl());
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
