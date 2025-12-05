package com.ssafy.dash.github.domain.exception;

public class GitHubFileDownloadException extends GitHubClientException {

    public GitHubFileDownloadException(String message) {
        super(message);
    }

    public GitHubFileDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

}
