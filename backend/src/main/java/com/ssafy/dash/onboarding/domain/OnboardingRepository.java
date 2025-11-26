package com.ssafy.dash.onboarding.domain;

import java.time.LocalDateTime;

public class OnboardingRepository {

    private Long id;
    private Long userId;
    private String repositoryName;
    private boolean webhookConfigured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OnboardingRepository() {}

    public OnboardingRepository(Long id, Long userId, String repositoryName, boolean webhookConfigured,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.repositoryName = repositoryName;
        this.webhookConfigured = webhookConfigured;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public boolean isWebhookConfigured() {
        return webhookConfigured;
    }

    public void setWebhookConfigured(boolean webhookConfigured) {
        this.webhookConfigured = webhookConfigured;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
}
