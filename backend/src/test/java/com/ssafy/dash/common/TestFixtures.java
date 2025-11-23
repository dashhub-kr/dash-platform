package com.ssafy.dash.common;

import java.time.LocalDateTime;

import com.ssafy.dash.board.domain.Board;
import com.ssafy.dash.board.dto.BoardCreateRequest;
import com.ssafy.dash.board.dto.BoardResponse;
import com.ssafy.dash.board.dto.BoardUpdateRequest;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.dto.UserCreateRequest;
import com.ssafy.dash.user.dto.UserUpdateRequest;

public class TestFixtures {

    public static final Long TEST_USER_ID = 1L;
    public static final String TEST_USERNAME = "tester";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PROVIDER = "github";
    public static final String TEST_PROVIDER_ID = "12345678";
    public static final String TEST_AVATAR_URL = "http://avatar.url";

    public static final Long TEST_BOARD_ID = 1L;
    public static final String TEST_BOARD_TITLE = "Test Title";
    public static final String TEST_BOARD_CONTENT = "Test Content";

    public static User createUser() {
        return new User(TEST_USER_ID, TEST_USERNAME, TEST_EMAIL, LocalDateTime.now(), TEST_PROVIDER, TEST_PROVIDER_ID, TEST_AVATAR_URL);
    }

    public static UserCreateRequest createUserCreateRequest() {
        return new UserCreateRequest(TEST_USERNAME, TEST_EMAIL);
    }

    public static UserUpdateRequest createUserUpdateRequest() {
        return new UserUpdateRequest("Updated User", "updated@example.com");
    }

    public static Board createBoard(User user) {
        return new Board(TEST_BOARD_ID, TEST_BOARD_TITLE, TEST_BOARD_CONTENT, user.getId(), LocalDateTime.now(), LocalDateTime.now());
    }

    public static BoardCreateRequest createBoardCreateRequest() {
        return new BoardCreateRequest(TEST_BOARD_TITLE, TEST_BOARD_CONTENT, TEST_USER_ID);
    }

    public static BoardUpdateRequest createBoardUpdateRequest() {
        return new BoardUpdateRequest("Updated Title", "Updated Content");
    }

    public static BoardResponse createBoardResponse(User user) {
        return new BoardResponse(TEST_BOARD_ID, TEST_BOARD_TITLE, TEST_BOARD_CONTENT, user.getId(), user.getUsername(), LocalDateTime.now(), LocalDateTime.now());
    }
}
