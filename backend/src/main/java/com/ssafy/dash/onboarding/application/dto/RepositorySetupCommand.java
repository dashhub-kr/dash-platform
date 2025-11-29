package com.ssafy.dash.onboarding.application.dto;

public class RepositorySetupCommand {

    private final Long userId;
    private final String repositoryName;

    public RepositorySetupCommand(Long userId, String repositoryName) {
        this.userId = userId;
        this.repositoryName = repositoryName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRepositoryName() {
        return repositoryName;
    }
    
}
