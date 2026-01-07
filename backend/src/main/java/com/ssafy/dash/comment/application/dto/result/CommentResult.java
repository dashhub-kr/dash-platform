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
        String authorName,
        String authorProfileImageUrl,
        String authorRole,
        Long parentId,
        Integer lineNumber,
        String content,
        Integer likeCount,
        boolean isLiked,
        List<CommentResult> replies,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CommentResult from(Comment comment, String authorName, String authorProfileImageUrl, String authorRole) {
        return new CommentResult(
                comment.getId(),
                comment.getBoardId(),
                comment.getUserId(),
                authorName,
                authorProfileImageUrl,
                authorRole,
                comment.getParentId(),
                comment.getLineNumber(),
                comment.getContent(),
                comment.getLikeCount() != null ? comment.getLikeCount() : 0,
                comment.isLiked(),
                Collections.emptyList(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

    public static CommentResult from(Comment comment, String authorName, String authorProfileImageUrl, String authorRole,
            List<CommentResult> replies) {
        return new CommentResult(
                comment.getId(),
                comment.getBoardId(),
                comment.getUserId(),
                authorName,
                authorProfileImageUrl,
                authorRole,
                comment.getParentId(),
                comment.getLineNumber(),
                comment.getContent(),
                comment.getLikeCount() != null ? comment.getLikeCount() : 0,
                comment.isLiked(),
                replies,
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

}
