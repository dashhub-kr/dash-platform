package com.ssafy.dash.common.fixtures;

import java.time.LocalDateTime;

import com.ssafy.dash.board.application.dto.command.BoardCreateCommand;
import com.ssafy.dash.board.application.dto.command.BoardUpdateCommand;
import com.ssafy.dash.board.application.dto.result.BoardResult;
import com.ssafy.dash.board.domain.Board;
import com.ssafy.dash.board.presentation.dto.request.BoardCreateRequest;
import com.ssafy.dash.board.presentation.dto.request.BoardUpdateRequest;
import com.ssafy.dash.user.domain.User;

public final class BoardFixtures {

    public static final Long TEST_BOARD_ID = 1L;
    public static final String TEST_BOARD_TITLE = "Test Title";
    public static final String TEST_BOARD_CONTENT = "Test Content";
    public static final String TEST_BOARD_TYPE = "GENERAL";
    public static final String UPDATED_BOARD_TITLE = "Updated Title";
    public static final String UPDATED_BOARD_CONTENT = "Updated Content";

    private BoardFixtures() {
    }

    public static Board createBoard(User user) {
        return boardFixture(user).toDomain(FixtureTime.now());
    }

    public static BoardCreateRequest createBoardCreateRequest(Long userId) {
        return boardFixture(userId).toCreateRequest();
    }

    public static BoardUpdateRequest createBoardUpdateRequest() {
        return new BoardUpdateRequest(UPDATED_BOARD_TITLE, UPDATED_BOARD_CONTENT, null);
    }

    public static BoardCreateCommand createBoardCreateCommand(Long userId) {
        return boardFixture(userId).toCreateCommand();
    }

    public static BoardUpdateCommand createBoardUpdateCommand() {
        return new BoardUpdateCommand(UPDATED_BOARD_TITLE, UPDATED_BOARD_CONTENT, null);
    }

    public static BoardResult createBoardResult(User user) {
        LocalDateTime timestamp = FixtureTime.now();
        return boardFixture(user).toResult(timestamp, timestamp);
    }

    public static BoardFixture boardFixture(User user) {
        return new BoardFixture(TEST_BOARD_ID, user.getId(), user.getUsername(),
                TEST_BOARD_TITLE, TEST_BOARD_CONTENT, TEST_BOARD_TYPE);
    }

    public static BoardFixture boardFixture(Long userId) {
        return new BoardFixture(TEST_BOARD_ID, userId, UserFixtures.TEST_USERNAME,
                TEST_BOARD_TITLE, TEST_BOARD_CONTENT, TEST_BOARD_TYPE);
    }

    public record BoardFixture(
            Long id,
            Long userId,
            String authorName,
            String title,
            String content,
            String boardType) {

        public Board toDomain(LocalDateTime timestamp) {
            Board board = Board.create(userId, title, content, null, boardType, timestamp);
            board.setId(id);
            return board;
        }

        public BoardCreateCommand toCreateCommand() {
            return new BoardCreateCommand(title, content, userId, null, boardType, null);
        }

        public BoardCreateRequest toCreateRequest() {
            return new BoardCreateRequest(title, content, userId, null, boardType, null);
        }

        public BoardResult toResult(LocalDateTime createdAt, LocalDateTime updatedAt) {
            return new BoardResult(id, title, content, userId, authorName, null, null, null, null, boardType, 0, 0,
                    false, null, createdAt,
                    updatedAt);
        }

    }

}
