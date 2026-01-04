package com.ssafy.dash.github.presentation;

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

import com.ssafy.dash.github.application.GitHubPushService;
import com.ssafy.dash.github.domain.WebhookSignatureValidator;
import com.ssafy.dash.github.domain.exception.GitHubWebhookException;

@RestController
@RequestMapping("/api/webhooks/github")
public class GitHubWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GitHubWebhookController.class);

    private final WebhookSignatureValidator signatureValidator;
    private final GitHubPushService pushService;

    public GitHubWebhookController(WebhookSignatureValidator signatureValidator, GitHubPushService pushService) {
        this.signatureValidator = signatureValidator;
        this.pushService = pushService;
    }

    @PostMapping
    public ResponseEntity<?> handleWebhook(
            @RequestHeader(name = "X-GitHub-Delivery", required = false) String deliveryId,
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

        if (!"push".equalsIgnoreCase(event)) {
            log.info("Unsupported GitHub event {}, ignoring", event);
            return ResponseEntity.accepted().build();
        }

        if (!StringUtils.hasText(deliveryId)) {
            log.warn("Missing X-GitHub-Delivery header for push event");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Delivery ID가 필요합니다.");
        }

        log.info("Accepted GitHub push event {}", deliveryId);
        try {
            pushService.handlePushEvent(deliveryId, body);
        } catch (Exception e) {
            log.error("Failed to process push event {}: {}", deliveryId, e.getMessage(), e);
            // 웹훅 처리에 실패하더라도 GitHub에는 성공으로 응답하여 재시도 방지
        }
        return ResponseEntity.accepted().build();
    }

}
