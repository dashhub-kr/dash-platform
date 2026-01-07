package com.ssafy.dash.board.application.dto.result;

import java.time.LocalDateTime;

import com.ssafy.dash.board.domain.Board;

public record BoardResult(
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

    public static BoardResult from(Board board, String authorName, String authorProfileImageUrl,
            String studyName, Integer problemNumber, Boolean isLiked, String authorDecorationClass) {
        return new BoardResult(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getUserId(),
                authorName,
                authorProfileImageUrl,
                studyName,
                board.getAlgorithmRecordId(),
                problemNumber,
                board.getBoardType(),
                board.getLikeCount() != null ? board.getLikeCount() : 0,
                board.getCommentCount() != null ? board.getCommentCount() : 0,
                isLiked != null ? isLiked : false,
                isLiked != null ? isLiked : false,
                board.getAuthorRole(),
                authorDecorationClass,
                board.getCreatedAt(),
                board.getUpdatedAt());
    }

}
