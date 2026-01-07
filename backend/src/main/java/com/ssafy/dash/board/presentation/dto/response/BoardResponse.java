package com.ssafy.dash.board.presentation.dto.response;

import java.time.LocalDateTime;

import com.ssafy.dash.board.application.dto.result.BoardResult;

public record BoardResponse(
        Long id,
        String title,
        String content,
        Long userId,
        String authorName,
        String authorProfileImageUrl,
        String studyName,
        Long algorithmRecordId,
        Integer problemNumber,
        String boardType,
        Integer likeCount,
        Integer commentCount,
        Boolean isLiked,
        Boolean isLiked,
        String authorRole,
        String authorDecorationClass,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static BoardResponse from(BoardResult result) {
        return new BoardResponse(
                result.id(),
                result.title(),
                result.content(),
                result.userId(),
                result.authorName(),
                result.authorProfileImageUrl(),
                result.studyName(),
                result.algorithmRecordId(),
                result.problemNumber(),
                result.boardType(),
                result.likeCount(),
                result.commentCount(),
                result.isLiked(),
                result.isLiked(),
                result.authorRole(),
                result.authorDecorationClass(),
                result.createdAt(),
                result.updatedAt());
    }

}
