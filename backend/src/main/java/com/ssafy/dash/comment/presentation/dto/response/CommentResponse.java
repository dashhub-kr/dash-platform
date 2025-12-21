package com.ssafy.dash.comment.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.ssafy.dash.comment.application.dto.result.CommentResult;

public record CommentResponse(
        Long id,
        Long boardId,
        Long userId,
        String authorName,
        Long parentId,
        Integer lineNumber,
        String content,
        Integer likeCount,
        List<CommentResponse> replies,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CommentResponse from(CommentResult result) {
        List<CommentResponse> repliesResponse = result.replies() != null
                ? result.replies().stream().map(CommentResponse::from).collect(Collectors.toList())
                : List.of();

        return new CommentResponse(
                result.id(),
                result.boardId(),
                result.userId(),
                result.authorName(),
                result.parentId(),
                result.lineNumber(),
                result.content(),
                result.likeCount(),
                repliesResponse,
                result.createdAt(),
                result.updatedAt());
    }

}
