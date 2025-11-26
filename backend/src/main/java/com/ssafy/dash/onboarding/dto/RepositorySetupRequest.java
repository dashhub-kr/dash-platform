package com.ssafy.dash.onboarding.dto;

import jakarta.validation.constraints.NotBlank;

public class RepositorySetupRequest {

    @NotBlank(message = "저장소 이름을 입력해주세요.")
    private String repositoryName;

    public RepositorySetupRequest() {}

    public RepositorySetupRequest(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
    
}
