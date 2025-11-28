package com.ssafy.dash.algorithm.application.dto;

import org.springframework.web.multipart.MultipartFile;

public class AlgorithmRecordCreateRequest {
    
    private String problemNumber;
    private String title;
    private String language;
    private MultipartFile file;

    public AlgorithmRecordCreateRequest() {}

    public AlgorithmRecordCreateRequest(String problemNumber, String title, String language, MultipartFile file) {
        this.problemNumber = problemNumber;
        this.title = title;
        this.language = language;
        this.file = file;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
    
}
