package com.ssafy.dash.algorithm.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlgorithmRecord {

    private Long id;
    private Long userId;
    private Long studyId;
    private String problemNumber;
    private String title;
    private String language;
    private String code;
    private String platform;
    private String difficulty;
    private Integer runtimeMs;
    private Integer memoryKb;
    private String repositoryName;
    private String filePath;
    private String commitSha;
    private String commitMessage;
    private LocalDateTime committedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String recordType; // USER_SOLUTION or TOP100_PROBLEM
    private String tag; // MISSION, MOCK_EXAM, DEFENSE, GENERAL
    private Integer defenseStreak;

    // Joined fields
    private String username;
    // Analysis Join fields
    private Integer score;
    private String timeComplexity;
    private String spaceComplexity;
    private String complexityExplanation;
    private String patterns;
    private String algorithmIntuition;
    private String pitfalls;
    private String keyBlocks;
    private Boolean refactorProvided;
    private String refactorCode;
    private String refactorExplanation;

    private AlgorithmRecord(Long userId, Long studyId, String problemNumber, String title, String language, String code,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = requirePositive(userId);
        this.studyId = studyId;
        this.problemNumber = requireText(problemNumber, "problemNumber");
        this.title = requireText(title, "title");
        this.language = requireText(language, "language");
        this.code = code == null ? "" : code;
        this.platform = "UNKNOWN";
        this.recordType = "USER_SOLUTION"; // Default type
        this.createdAt = requireTimestamp(createdAt, "createdAt");
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    public static AlgorithmRecord create(Long userId, Long studyId, String problemNumber, String title, String language,
            String code, LocalDateTime createdAt) {
        return new AlgorithmRecord(
                userId,
                studyId,
                problemNumber,
                title,
                language,
                code,
                createdAt,
                createdAt);
    }

    public void applyUpdate(String problemNumber, String title, String language, String code, LocalDateTime updatedAt) {
        if (problemNumber != null && !problemNumber.isBlank()) {
            this.problemNumber = problemNumber;
        }
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (language != null && !language.isBlank()) {
            this.language = language;
        }
        if (code != null) {
            this.code = code;
        }
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    public void enrichMetadata(String platform, String difficulty, Integer runtimeMs, Integer memoryKb,
            String repositoryName, String filePath, String commitSha, String commitMessage, LocalDateTime committedAt) {
        if (platform != null && !platform.isBlank()) {
            this.platform = platform;
        }
        this.difficulty = difficulty;
        this.runtimeMs = runtimeMs;
        this.memoryKb = memoryKb;
        this.repositoryName = repositoryName;
        this.filePath = filePath;
        this.commitSha = commitSha;
        this.commitMessage = commitMessage;
        this.committedAt = committedAt;
    }

    private static Long requirePositive(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("userId" + " must be positive");
        }
        return value;
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
