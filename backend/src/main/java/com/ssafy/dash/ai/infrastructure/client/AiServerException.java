package com.ssafy.dash.ai.infrastructure.client;

/**
 * AI 서버 통신 예외
 */
public class AiServerException extends RuntimeException {

    public AiServerException(String message) {
        super(message);
    }

    public AiServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
