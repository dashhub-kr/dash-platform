package com.ssafy.dash.board.presentation.dto.response;

import java.time.LocalDateTime;

import com.ssafy.dash.board.application.dto.BoardResult;

public class BoardResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final Long userId;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BoardResponse(Long id, String title, String content, Long userId, String authorName,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BoardResponse from(BoardResult result) {
        return new BoardResponse(result.getId(), result.getTitle(), result.getContent(), result.getUserId(),
                result.getAuthorName(), result.getCreatedAt(), result.getUpdatedAt());
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
}
