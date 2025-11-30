package com.ssafy.dash.algorithm.application.dto;

import java.time.LocalDateTime;

public class AlgorithmRecordResult {

    private final Long id;
    private final Long userId;
    private final String problemNumber;
    private final String title;
    private final String code;
    private final String language;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AlgorithmRecordResult(Long id, Long userId, String problemNumber, String title,
            String code, String language, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.problemNumber = problemNumber;
        this.title = title;
        this.code = code;
        this.language = language;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getProblemNumber() {
        return problemNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    public String getLanguage() {
        return language;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
