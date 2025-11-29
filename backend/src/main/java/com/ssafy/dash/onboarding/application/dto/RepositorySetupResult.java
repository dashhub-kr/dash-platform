package com.ssafy.dash.onboarding.application.dto;

public class RepositorySetupResult {

    private final Long userId;
    private final String repositoryName;
    private final boolean webhookConfigured;

    public RepositorySetupResult(Long userId, String repositoryName, boolean webhookConfigured) {
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
