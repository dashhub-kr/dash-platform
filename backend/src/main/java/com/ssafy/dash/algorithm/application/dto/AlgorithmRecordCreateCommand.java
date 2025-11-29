package com.ssafy.dash.algorithm.application.dto;

public class AlgorithmRecordCreateCommand {

    private final Long userId;
    private final String problemNumber;
    private final String title;
    private final String language;
    private final String code;

    public AlgorithmRecordCreateCommand(Long userId, String problemNumber, String title,
            String language, String code) {
        this.userId = userId;
        this.problemNumber = problemNumber;
        this.title = title;
        this.language = language;
        this.code = code;
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

    public String getLanguage() {
        return language;
    }

    public String getCode() {
        return code;
    }

}
