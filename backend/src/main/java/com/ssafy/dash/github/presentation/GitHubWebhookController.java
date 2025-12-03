package com.ssafy.dash.github.presentation;

import com.ssafy.dash.github.domain.WebhookSignatureValidator;
import com.ssafy.dash.github.domain.exception.GitHubWebhookException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/github")
public class GitHubWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GitHubWebhookController.class);

    private final WebhookSignatureValidator signatureValidator;

    public GitHubWebhookController(WebhookSignatureValidator signatureValidator) {
        this.signatureValidator = signatureValidator;
    }

    @PostMapping
    public ResponseEntity<?> handleWebhook(
            @RequestHeader(name = "X-GitHub-Event", required = false) String event,
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody(required = false) String payload) {
        String body = payload == null ? "" : payload;

        try {
            if (!signatureValidator.isValid(signature, body)) {
                log.warn("Invalid GitHub webhook signature. Event: {}, Signature: {}", event, signature);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("GitHub webhook 서명이 유효하지 않습니다.");
            }
        } catch (GitHubWebhookException ex) {
            log.error("GitHub webhook signature validation failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("GitHub 웹훅 처리 중 오류가 발생했습니다.");
        }

        if ("ping".equalsIgnoreCase(event)) {
            log.info("Received GitHub webhook ping");
            return ResponseEntity.ok().build();
        }

        log.info("Received GitHub webhook event: {}", event);
        return ResponseEntity.accepted().build();
    }

}
