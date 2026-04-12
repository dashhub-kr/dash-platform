package com.ssafy.dash.onboarding.domain.exception;

import com.ssafy.dash.common.exception.BusinessException;
import com.ssafy.dash.common.exception.ErrorCode;

public class GitHubAppNotInstalledException extends BusinessException {
    public GitHubAppNotInstalledException(String repositoryName) {
        super(ErrorCode.GITHUB_APP_NOT_INSTALLED, "GitHub App is not installed on repository: " + repositoryName);
    }
}
