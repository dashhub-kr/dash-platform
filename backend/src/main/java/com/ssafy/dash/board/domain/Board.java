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
    private Long algorithmRecordId; // 연결된 알고리즘 풀이 기록 ID (nullable)
    private String boardType; // GENERAL | CODE_REVIEW
    private Integer likeCount; // 추천 수 (캐시)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 조인 필드
    private String authorName;
    private Integer commentCount;
    private String authorRole;

    private Board(Long userId, String title, String content, Long algorithmRecordId,
            String boardType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = requirePositive(userId);
        this.title = requireText(title, "title");
        this.content = requireText(content, "content");
        this.algorithmRecordId = algorithmRecordId;
        this.boardType = boardType != null ? boardType : "GENERAL";
        this.likeCount = 0;
        this.createdAt = requireTimestamp(createdAt, "createdAt");
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    public static Board create(Long userId, String title, String content,
            Long algorithmRecordId, String boardType,
            LocalDateTime createdAt) {
        return new Board(userId, title, content, algorithmRecordId, boardType, createdAt, createdAt);
    }

    public void applyUpdate(String title, String content, Long algorithmRecordId, LocalDateTime updatedAt) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        this.algorithmRecordId = algorithmRecordId;
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }

    public void decrementLikeCount() {
        this.likeCount = Math.max(0, (this.likeCount == null ? 0 : this.likeCount) - 1);
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
