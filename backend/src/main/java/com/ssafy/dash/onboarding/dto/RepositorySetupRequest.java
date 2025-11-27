package com.ssafy.dash.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RepositorySetupRequest {

    @NotBlank(message = "저장소 이름을 입력해주세요.")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$", message = "owner/repository 형식으로 입력해주세요.")
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
