package com.ssafy.dash.user.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String username;
    private String email;
    private String role;
    @Builder.Default
    private String status = "ACTIVE";
    private LocalDateTime createdAt;
    private String provider;
    private String providerId;
    private String avatarUrl;
    private Long studyId;
    private Long equippedDecorationId;
    private String equippedDecorationClass; // Join Field
    private LocalDateTime deletedAt;

    // Solved.ac 연동 필드
    private String solvedacHandle;
    private Integer solvedacTier;
    private Integer solvedacRating;
    private Integer solvedacClass;
    @Builder.Default
    private Integer solvedCount = 0;
    @Builder.Default
    private Integer logCount = 0; // Personal Currency
    private LocalDateTime statsLastSyncedAt;

    public void addLogs(int amount) {
        if (amount > 0) {
            this.logCount = (this.logCount == null ? 0 : this.logCount) + amount;
        }
    }

    public void useLogs(int amount) {
        if (amount > 0) {
            int current = (this.logCount == null ? 0 : this.logCount);
            if (current < amount) {
                throw new IllegalStateException("Not enough logs");
            }
            this.logCount = current - amount;
        }
    }

    // 랜덤 디펜스 필드
    private String defenseType; // "SILVER" 또는 "GOLD"
    private Integer defenseProblemId;
    private LocalDateTime defenseStartTime;
    @Builder.Default
    private Integer silverStreak = 0;
    @Builder.Default
    private Integer goldStreak = 0;
    @Builder.Default
    private Integer maxSilverStreak = 0;
    @Builder.Default
    private Integer maxGoldStreak = 0;

    private User(Long id, String username, String email, LocalDateTime createdAt,
            String provider, String providerId, String avatarUrl) {
        this.id = id;
        this.username = requireText(username, "username");
        this.email = requireText(email, "email");
        this.role = "ROLE_USER";
        this.status = "ACTIVE";
        this.createdAt = requireTimestamp(createdAt);
        this.provider = provider;
        this.providerId = providerId;
        this.avatarUrl = avatarUrl;
    }

    public void markDeleted(LocalDateTime deletedAt) {
        this.deletedAt = Objects.requireNonNullElse(deletedAt, LocalDateTime.now());
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public static User create(String username, String email, LocalDateTime createdAt,
            String provider, String providerId, String avatarUrl) {
        return new User(null, username, email, createdAt, provider, providerId, avatarUrl);
    }

    public void updateProfile(String username, String email, String avatarUrl) {
        this.username = requireText(username, "username");
        this.email = requireText(email, "email");
        this.avatarUrl = avatarUrl;
    }

    public void updateStudy(Long studyId) {
        this.studyId = studyId;
    }

    public void updateSolvedacProfile(String handle, Integer tier, Integer rating, Integer classLevel,
            Integer solvedCount) {
        this.solvedacHandle = handle;
        this.solvedacTier = tier;
        this.solvedacRating = rating;
        this.solvedacClass = classLevel;
        this.solvedCount = solvedCount;
        this.statsLastSyncedAt = LocalDateTime.now();
    }

    public void block() {
        this.status = "BLOCKED";
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static LocalDateTime requireTimestamp(LocalDateTime value) {
        return Objects.requireNonNull(value, "createdAt" + " must not be null");
    }

}
