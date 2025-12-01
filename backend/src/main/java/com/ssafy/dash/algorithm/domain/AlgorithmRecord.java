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
    private String problemNumber;
    private String title;
    private String language;
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private AlgorithmRecord(Long userId, String problemNumber, String title, String language, String code, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = requirePositive(userId, "userId");
        this.problemNumber = requireText(problemNumber, "problemNumber");
        this.title = requireText(title, "title");
        this.language = requireText(language, "language");
        this.code = code == null ? "" : code;
        this.createdAt = requireTimestamp(createdAt, "createdAt");
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    public static AlgorithmRecord create(Long userId, String problemNumber, String title, String language, String code, LocalDateTime createdAt) {
        return new AlgorithmRecord(userId, problemNumber, title, language, code, createdAt, createdAt);
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

    private static Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
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
