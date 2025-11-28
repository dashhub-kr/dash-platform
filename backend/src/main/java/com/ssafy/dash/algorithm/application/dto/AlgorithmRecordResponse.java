package com.ssafy.dash.algorithm.application.dto;

import java.time.LocalDateTime;

public class AlgorithmRecordResponse {

    private Long id;
    private Long userId;
    private String problemNumber;
    private String title;
    private String code;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AlgorithmRecordResponse() {}

    public AlgorithmRecordResponse(Long id, Long userId, String problemNumber, String title, String code, String language, LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProblemNumber() {
        return problemNumber;
    }

    public void setProblemNumber(String problemNumber) {
        this.problemNumber = problemNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
}
