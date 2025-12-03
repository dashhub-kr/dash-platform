package com.ssafy.dash.board.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Board {

    private Long id;
    private String title;
    private String content;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Board(Long userId, String title, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = requirePositive(userId);
        this.title = requireText(title, "title");
        this.content = requireText(content, "content");
        this.createdAt = requireTimestamp(createdAt, "createdAt");
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    public static Board create(Long userId, String title, String content, LocalDateTime createdAt) {
        return new Board(userId, title, content, createdAt, createdAt);
    }

    public void applyUpdate(String title, String content, LocalDateTime updatedAt) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    private static Long requirePositive(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("userId" + " must be positive");
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
