package com.ssafy.dash.common;

import java.time.LocalDateTime;

import com.ssafy.dash.board.application.dto.BoardCreateCommand;
import com.ssafy.dash.board.application.dto.BoardResult;
import com.ssafy.dash.board.application.dto.BoardUpdateCommand;
import com.ssafy.dash.board.domain.Board;
import com.ssafy.dash.board.presentation.dto.request.BoardCreateRequest;
import com.ssafy.dash.board.presentation.dto.request.BoardUpdateRequest;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.application.dto.UserCreateRequest;
import com.ssafy.dash.user.application.dto.UserUpdateRequest;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordCreateRequest;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordResponse;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

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

    public static final Long TEST_ALGORITHM_RECORD_ID = 1L;
    public static final String TEST_PROBLEM_NUMBER = "1000";
    public static final String TEST_ALGORITHM_TITLE = "A+B";
    public static final String TEST_ALGORITHM_CODE = "import java.util.*; ...";
    public static final String TEST_ALGORITHM_LANGUAGE = "Java";

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

    public static BoardCreateCommand createBoardCreateCommand() {
        return new BoardCreateCommand(TEST_BOARD_TITLE, TEST_BOARD_CONTENT, TEST_USER_ID);
    }

    public static BoardUpdateCommand createBoardUpdateCommand() {
        return new BoardUpdateCommand("Updated Title", "Updated Content");
    }

    public static BoardResult createBoardResult(User user) {
        return new BoardResult(TEST_BOARD_ID, TEST_BOARD_TITLE, TEST_BOARD_CONTENT,
                user.getId(), user.getUsername(), LocalDateTime.now(), LocalDateTime.now());
    }

    public static AlgorithmRecord createAlgorithmRecord(User user) {
        return new AlgorithmRecord(TEST_ALGORITHM_RECORD_ID, user.getId(), TEST_PROBLEM_NUMBER, TEST_ALGORITHM_TITLE, TEST_ALGORITHM_CODE, TEST_ALGORITHM_LANGUAGE, LocalDateTime.now(), LocalDateTime.now());
    }

    public static AlgorithmRecordCreateRequest createAlgorithmRecordCreateRequest(MultipartFile file) {
        return new AlgorithmRecordCreateRequest(TEST_PROBLEM_NUMBER, TEST_ALGORITHM_TITLE, TEST_ALGORITHM_LANGUAGE, file);
    }

    public static AlgorithmRecordUpdateRequest createAlgorithmRecordUpdateRequest(MultipartFile file) {
        return new AlgorithmRecordUpdateRequest(TEST_PROBLEM_NUMBER, "Updated Title", TEST_ALGORITHM_LANGUAGE, file);
    }

    public static AlgorithmRecordResponse createAlgorithmRecordResponse(User user) {
        return new AlgorithmRecordResponse(TEST_ALGORITHM_RECORD_ID, user.getId(), TEST_PROBLEM_NUMBER, TEST_ALGORITHM_TITLE, TEST_ALGORITHM_CODE, TEST_ALGORITHM_LANGUAGE, LocalDateTime.now(), LocalDateTime.now());
    }
    
}
