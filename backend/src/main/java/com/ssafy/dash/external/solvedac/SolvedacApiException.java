package com.ssafy.dash.external.solvedac;

/**
 * Solved.ac API 호출 중 발생하는 예외
 */
public class SolvedacApiException extends RuntimeException {
    public SolvedacApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public SolvedacApiException(String message) {
        super(message);
    }
}
