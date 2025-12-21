package com.ssafy.dash.comment.application.dto.result;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.ssafy.dash.comment.domain.Comment;

public record CommentResult(
        Long id,
        Long boardId,
        Long userId,
        String authorName,
        Long parentId,
        Integer lineNumber,
        String content,
        Integer likeCount,
        List<CommentResult> replies,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CommentResult from(Comment comment, String authorName) {
        return new CommentResult(
                comment.getId(),
                comment.getBoardId(),
                comment.getUserId(),
                authorName,
                comment.getParentId(),
                comment.getLineNumber(),
                comment.getContent(),
                comment.getLikeCount() != null ? comment.getLikeCount() : 0,
                Collections.emptyList(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

    public static CommentResult from(Comment comment, String authorName, List<CommentResult> replies) {
        return new CommentResult(
                comment.getId(),
                comment.getBoardId(),
                comment.getUserId(),
                authorName,
                comment.getParentId(),
                comment.getLineNumber(),
                comment.getContent(),
                comment.getLikeCount() != null ? comment.getLikeCount() : 0,
                replies,
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

}
