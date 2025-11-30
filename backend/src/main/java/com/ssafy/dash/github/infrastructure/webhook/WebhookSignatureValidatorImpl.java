package com.ssafy.dash.github.infrastructure.webhook;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ssafy.dash.github.config.GitHubWebhookProperties;
import com.ssafy.dash.github.domain.WebhookSignatureValidator;
import com.ssafy.dash.github.domain.exception.GitHubWebhookException;

@Component
public class WebhookSignatureValidatorImpl implements WebhookSignatureValidator {

    private static final Logger log = LoggerFactory.getLogger(WebhookSignatureValidatorImpl.class);

    private final GitHubWebhookProperties properties;

    public WebhookSignatureValidatorImpl(GitHubWebhookProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isValid(String signature, String payload) {
        String secret = properties.getSecret();
        if (!StringUtils.hasText(secret)) {
            log.error("GitHub webhook secret is not configured");

            return false;
        }
        if (!StringUtils.hasText(signature) || !signature.startsWith("sha256=")) {
            
            return false;
        }
        String computed = "sha256=" + hmacSha256Hex(secret, payload);
        
        return constantTimeEquals(computed, signature);
    }

    private String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            return toHex(digest);
        } catch (Exception ex) {
            log.error("Failed to verify GitHub webhook signature", ex);
            
            throw new GitHubWebhookException("웹훅 서명 검증 중 오류가 발생했습니다.", ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }

    private boolean constantTimeEquals(String expected, String actual) {
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = actual.getBytes(StandardCharsets.UTF_8);
        if (a.length != b.length) {

            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }

        return result == 0;
    }
    
}
