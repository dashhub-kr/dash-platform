package com.ssafy.dash.algorithm.presentation.dto.response;

import java.time.LocalDateTime;

import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordResult;

public class AlgorithmRecordResponse {

    private final Long id;
    private final Long userId;
    private final String problemNumber;
    private final String title;
    private final String code;
    private final String language;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AlgorithmRecordResponse(Long id, Long userId, String problemNumber, String title,
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

    public static AlgorithmRecordResponse from(AlgorithmRecordResult result) {
        return new AlgorithmRecordResponse(result.getId(), result.getUserId(),
                result.getProblemNumber(), result.getTitle(), result.getCode(),
                result.getLanguage(), result.getCreatedAt(), result.getUpdatedAt());
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
