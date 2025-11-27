package com.ssafy.dash.onboarding.github;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/webhooks/github")
public class GitHubWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GitHubWebhookController.class);

    private final GitHubWebhookProperties properties;

    public GitHubWebhookController(GitHubWebhookProperties properties) {
        this.properties = properties;
    }

    @PostMapping
    public ResponseEntity<?> handleWebhook(
            @RequestHeader(name = "X-GitHub-Event", required = false) String event,
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody(required = false) String payload) {
        String body = payload == null ? "" : payload;

        if (!isSignatureValid(signature, body)) {
            log.warn("Invalid GitHub webhook signature. Event: {}, Signature: {}", event, signature);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("GitHub webhook 서명이 유효하지 않습니다.");
        }

        if ("ping".equalsIgnoreCase(event)) {
            log.info("Received GitHub webhook ping");

            return ResponseEntity.ok().build();
        }

        log.info("Received GitHub webhook event: {}", event);

        return ResponseEntity.accepted().build();
    }

    private boolean isSignatureValid(String signature, String payload) {
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

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "웹훅 서명 검증 중 오류가 발생했습니다.");
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
