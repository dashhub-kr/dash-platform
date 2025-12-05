package com.ssafy.dash.github.domain.exception;

public class GitHubPayloadProcessingException extends GitHubClientException {

    public GitHubPayloadProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
