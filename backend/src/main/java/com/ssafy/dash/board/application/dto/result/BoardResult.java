package com.ssafy.dash.board.application.dto.result;

import java.time.LocalDateTime;

import com.ssafy.dash.board.domain.Board;

public record BoardResult(
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

    public static BoardResult from(Board board, String authorName, Boolean isLiked) {
        return new BoardResult(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getUserId(),
                authorName,
                board.getAlgorithmRecordId(),
                board.getBoardType(),
                board.getLikeCount() != null ? board.getLikeCount() : 0,
                board.getCommentCount() != null ? board.getCommentCount() : 0,
                isLiked != null ? isLiked : false,
                board.getCreatedAt(),
                board.getUpdatedAt());
    }

}
