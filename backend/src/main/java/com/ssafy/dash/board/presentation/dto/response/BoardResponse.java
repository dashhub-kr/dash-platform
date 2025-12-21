package com.ssafy.dash.board.presentation.dto.response;

import java.time.LocalDateTime;

import com.ssafy.dash.board.application.dto.result.BoardResult;

public record BoardResponse(
        Long id,
        String title,
        String content,
        Long userId,
        String authorName,
        Long algorithmRecordId,
        String boardType,
        Integer likeCount,
        Integer commentCount,
        Boolean isLiked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static BoardResponse from(BoardResult result) {
        return new BoardResponse(
                result.id(),
                result.title(),
                result.content(),
                result.userId(),
                result.authorName(),
                result.algorithmRecordId(),
                result.boardType(),
                result.likeCount(),
                result.commentCount(),
                result.isLiked(),
                result.createdAt(),
                result.updatedAt());
    }

}
