package com.ssafy.dash.board.presentation.dto.request;

import com.ssafy.dash.board.application.dto.BoardCreateCommand;

public class BoardCreateRequest {

    private String title;
    private String content;
    private Long userId;

    public BoardCreateRequest() {}

    public BoardCreateRequest(String title, String content, Long userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BoardCreateCommand toCommand() {
        return new BoardCreateCommand(title, content, userId);
    }
    
}
