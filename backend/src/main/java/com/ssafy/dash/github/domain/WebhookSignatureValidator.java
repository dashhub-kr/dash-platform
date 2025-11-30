package com.ssafy.dash.github.domain;

public interface WebhookSignatureValidator {
    boolean isValid(String signature, String payload);
}
