package com.ssafy.dash.onboarding.exception;

public class WebhookRegistrationException extends RuntimeException {

    public WebhookRegistrationException(String message) {
        super(message);
    }

    public WebhookRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
