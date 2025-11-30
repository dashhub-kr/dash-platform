package com.ssafy.dash.board.application.dto;

public class BoardUpdateCommand {

    private final String title;
    private final String content;

    public BoardUpdateCommand(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
    
}
