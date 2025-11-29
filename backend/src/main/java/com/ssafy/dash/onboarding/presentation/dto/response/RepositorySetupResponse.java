package com.ssafy.dash.onboarding.presentation.dto.response;

import com.ssafy.dash.onboarding.application.dto.RepositorySetupResult;

public class RepositorySetupResponse {

    private final Long userId;
    private final String repositoryName;
    private final boolean webhookConfigured;

    public RepositorySetupResponse(Long userId, String repositoryName, boolean webhookConfigured) {
        this.userId = userId;
        this.repositoryName = repositoryName;
        this.webhookConfigured = webhookConfigured;
    }

    public static RepositorySetupResponse from(RepositorySetupResult result) {
        return new RepositorySetupResponse(result.getUserId(), result.getRepositoryName(), result.isWebhookConfigured());
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
