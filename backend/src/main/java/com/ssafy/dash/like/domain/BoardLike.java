package com.ssafy.dash.like.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BoardLike {

    private Long id;
    private Long boardId;
    private Long userId;
    private LocalDateTime createdAt;

    protected BoardLike(Long boardId, Long userId, LocalDateTime createdAt) {
        this.boardId = boardId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static BoardLike create(Long boardId, Long userId, LocalDateTime createdAt) {
        return new BoardLike(boardId, userId, createdAt);
    }

}
