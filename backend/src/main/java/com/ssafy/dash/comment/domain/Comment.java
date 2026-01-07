package com.ssafy.dash.comment.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Comment {

    private Long id;
    private Long boardId;
    private Long userId;
    private Long parentId; // nullable - 대댓글용 (null = 최상위 댓글)
    private Integer lineNumber; // nullable - 코드 라인 댓글용 (null = 일반 댓글)
    private String content;
    private Integer likeCount; // 추천 수 (캐시)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 조인 필드
    private String authorName;
    private String authorName;
    private String authorProfileImageUrl;
    private String authorRole; // Join Field
    private String authorDecorationClass; // Join Field
    private List<Comment> replies; // 대댓글 목록
    private boolean isLiked; // 현재 사용자의 좋아요 여부 (조회용)

    private Comment(Long boardId, Long userId, Long parentId, Integer lineNumber,
            String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.boardId = requirePositive(boardId, "boardId");
        this.userId = requirePositive(userId, "userId");
        this.parentId = parentId;
        this.lineNumber = lineNumber;
        this.content = requireText(content, "content");
        this.likeCount = 0;
        this.createdAt = requireTimestamp(createdAt, "createdAt");
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    public static Comment create(Long boardId, Long userId, Long parentId,
            Integer lineNumber, String content,
            LocalDateTime createdAt) {
        return new Comment(boardId, userId, parentId, lineNumber, content, createdAt, createdAt);
    }

    public void applyUpdate(String content, LocalDateTime updatedAt) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        this.updatedAt = requireTimestamp(updatedAt, "updatedAt");
    }

    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }

    public void decrementLikeCount() {
        this.likeCount = Math.max(0, (this.likeCount == null ? 0 : this.likeCount) - 1);
    }

    public boolean isReply() {
        return this.parentId != null;
    }

    public boolean isLineComment() {
        return this.lineNumber != null;
    }

    private static Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
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
