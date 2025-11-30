package com.ssafy.dash.board.presentation.dto.request;

import com.ssafy.dash.board.application.dto.BoardUpdateCommand;

public class BoardUpdateRequest {

    private String title;
    private String content;

    public BoardUpdateRequest() {}

    public BoardUpdateRequest(String title, String content) {
        this.title = title;
        this.content = content;
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

    public BoardUpdateCommand toCommand() {
        return new BoardUpdateCommand(title, content);
    }
    
}
