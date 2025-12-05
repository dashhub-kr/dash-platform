package com.ssafy.dash.github.domain.exception;

/** 웹훅 이외의 GitHub 클라이언트 작업에서 발생하는 일반적인 예외 */
public class GitHubClientException extends RuntimeException {

    public GitHubClientException(String message) {
        super(message);
    }

    public GitHubClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
