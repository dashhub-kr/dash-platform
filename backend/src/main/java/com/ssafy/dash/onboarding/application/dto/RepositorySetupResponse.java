package com.ssafy.dash.onboarding.application.dto;

public class RepositorySetupResponse {

    private final Long userId;
    private final String repositoryName;
    private final boolean webhookConfigured;

    public RepositorySetupResponse(Long userId, String repositoryName, boolean webhookConfigured) {
        this.userId = userId;
        this.repositoryName = repositoryName;
        this.webhookConfigured = webhookConfigured;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public boolean isWebhookConfigured() {
        return webhookConfigured;
    }
    
}
