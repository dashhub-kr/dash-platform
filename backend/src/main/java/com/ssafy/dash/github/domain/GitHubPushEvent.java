package com.ssafy.dash.github.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GitHubPushEvent {

    private Long id;
    private String deliveryId;
    private String repositoryName;
    private String reference;
    private String headCommitSha;
    private String authorLogin;
    private String authorEmail;
    private String commitMessage;
    private String filesJson;
    private String rawPayload;
    private PushEventStatus status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private GitHubPushEvent(String deliveryId, String repositoryName, String reference, String headCommitSha, String authorLogin, String authorEmail, String commitMessage, String filesJson, String rawPayload, PushEventStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.deliveryId = requireText(deliveryId, "deliveryId");
        this.repositoryName = requireText(repositoryName, "repositoryName");
        this.reference = requireText(reference, "reference");
        this.headCommitSha = requireText(headCommitSha, "headCommitSha");
        this.authorLogin = authorLogin;
        this.authorEmail = authorEmail;
        this.commitMessage = commitMessage;
        this.filesJson = filesJson;
        this.rawPayload = rawPayload;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = requireTimestamp(createdAt, "createdAt");
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    public static GitHubPushEvent enqueue(String deliveryId, String repositoryName, String reference, String headCommitSha, String authorLogin, String authorEmail, String commitMessage, String filesJson, String rawPayload, LocalDateTime receivedAt) {
        return new GitHubPushEvent(deliveryId, repositoryName, reference, headCommitSha, authorLogin, authorEmail, commitMessage, filesJson, rawPayload, PushEventStatus.QUEUED, receivedAt, receivedAt
            );
    }

    public void markFailed(String reason, LocalDateTime updatedAt) {
        this.status = PushEventStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    public void markProcessing(LocalDateTime updatedAt) {
        this.status = PushEventStatus.PROCESSING;
        this.failureReason = null;
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    public void markCompleted(LocalDateTime updatedAt) {
        this.status = PushEventStatus.COMPLETED;
        this.failureReason = null;
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static LocalDateTime requireTimestamp(LocalDateTime value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }

}
