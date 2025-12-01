package com.ssafy.dash.common;

import java.time.LocalDateTime;

import com.ssafy.dash.algorithm.application.dto.command.AlgorithmRecordCreateCommand;
import com.ssafy.dash.algorithm.application.dto.command.AlgorithmRecordUpdateCommand;
import com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.board.application.dto.command.BoardCreateCommand;
import com.ssafy.dash.board.application.dto.command.BoardUpdateCommand;
import com.ssafy.dash.board.application.dto.result.BoardResult;
import com.ssafy.dash.board.domain.Board;
import com.ssafy.dash.board.presentation.dto.request.BoardCreateRequest;
import com.ssafy.dash.board.presentation.dto.request.BoardUpdateRequest;
import com.ssafy.dash.common.fixtures.AlgorithmRecordFixtures;
import com.ssafy.dash.common.fixtures.BoardFixtures;
import com.ssafy.dash.common.fixtures.FixtureTime;
import com.ssafy.dash.common.fixtures.OnboardingFixtures;
import com.ssafy.dash.common.fixtures.UserFixtures;
import com.ssafy.dash.onboarding.application.dto.command.RepositorySetupCommand;
import com.ssafy.dash.onboarding.application.dto.result.RepositorySetupResult;
import com.ssafy.dash.onboarding.domain.Onboarding;
import com.ssafy.dash.user.application.dto.command.UserCreateCommand;
import com.ssafy.dash.user.application.dto.command.UserUpdateCommand;
import com.ssafy.dash.user.application.dto.result.UserResult;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.presentation.dto.request.UserCreateRequest;
import com.ssafy.dash.user.presentation.dto.request.UserUpdateRequest;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static final Long TEST_USER_ID = UserFixtures.TEST_USER_ID;
    public static final String TEST_USERNAME = UserFixtures.TEST_USERNAME;
    public static final String TEST_EMAIL = UserFixtures.TEST_EMAIL;
    public static final String TEST_PROVIDER = UserFixtures.TEST_PROVIDER;
    public static final String TEST_PROVIDER_ID = UserFixtures.TEST_PROVIDER_ID;
    public static final String TEST_AVATAR_URL = UserFixtures.TEST_AVATAR_URL;
    public static final String UPDATED_USERNAME = UserFixtures.UPDATED_USERNAME;
    public static final String UPDATED_EMAIL = UserFixtures.UPDATED_EMAIL;

    public static final Long TEST_BOARD_ID = BoardFixtures.TEST_BOARD_ID;
    public static final String TEST_BOARD_TITLE = BoardFixtures.TEST_BOARD_TITLE;
    public static final String TEST_BOARD_CONTENT = BoardFixtures.TEST_BOARD_CONTENT;
    public static final String UPDATED_BOARD_TITLE = BoardFixtures.UPDATED_BOARD_TITLE;
    public static final String UPDATED_BOARD_CONTENT = BoardFixtures.UPDATED_BOARD_CONTENT;

    public static final Long TEST_ALGORITHM_RECORD_ID = AlgorithmRecordFixtures.TEST_ALGORITHM_RECORD_ID;
    public static final String TEST_PROBLEM_NUMBER = AlgorithmRecordFixtures.TEST_PROBLEM_NUMBER;
    public static final String TEST_ALGORITHM_TITLE = AlgorithmRecordFixtures.TEST_ALGORITHM_TITLE;
    public static final String TEST_ALGORITHM_CODE = AlgorithmRecordFixtures.TEST_ALGORITHM_CODE;
    public static final String TEST_ALGORITHM_LANGUAGE = AlgorithmRecordFixtures.TEST_ALGORITHM_LANGUAGE;

    public static final Long TEST_ONBOARDING_ID = OnboardingFixtures.TEST_ONBOARDING_ID;
    public static final String TEST_REPOSITORY_NAME = OnboardingFixtures.TEST_REPOSITORY_NAME;

    // Fixture Time
    public static LocalDateTime fixedNow() {
        return FixtureTime.now();
    }

    // User Fixtures
    public static User createUser() {
        return UserFixtures.createUser();
    }

    public static UserCreateRequest createUserCreateRequest() {
        return UserFixtures.createUserCreateRequest();
    }

    public static UserUpdateRequest createUserUpdateRequest() {
        return UserFixtures.createUserUpdateRequest();
    }

    public static UserCreateCommand createUserCreateCommand() {
        return UserFixtures.createUserCreateCommand();
    }

    public static UserUpdateCommand createUserUpdateCommand() {
        return UserFixtures.createUserUpdateCommand();
    }

    public static UserResult createUserResult() {
        return UserFixtures.createUserResult();
    }

    public static UserFixtures.UserFixture userFixture() {
        return UserFixtures.userFixture();
    }

    // Board Fixtures
    public static Board createBoard(User user) {
        return BoardFixtures.createBoard(user);
    }

    public static BoardCreateRequest createBoardCreateRequest() {
        return BoardFixtures.createBoardCreateRequest(TEST_USER_ID);
    }

    public static BoardUpdateRequest createBoardUpdateRequest() {
        return BoardFixtures.createBoardUpdateRequest();
    }

    public static BoardCreateCommand createBoardCreateCommand() {
        return BoardFixtures.createBoardCreateCommand(TEST_USER_ID);
    }

    public static BoardUpdateCommand createBoardUpdateCommand() {
        return BoardFixtures.createBoardUpdateCommand();
    }

    public static BoardResult createBoardResult(User user) {
        return BoardFixtures.createBoardResult(user);
    }

    public static BoardFixtures.BoardFixture boardFixture(User user) {
        return BoardFixtures.boardFixture(user);
    }

    public static BoardFixtures.BoardFixture boardFixture(Long userId) {
        return BoardFixtures.boardFixture(userId);
    }

    // AlgorithmRecord Fixtures
    public static AlgorithmRecord createAlgorithmRecord(User user) {
        return AlgorithmRecordFixtures.createAlgorithmRecord(user.getId());
    }

    public static AlgorithmRecordCreateCommand createAlgorithmRecordCreateCommand(String code) {
        return AlgorithmRecordFixtures.createAlgorithmRecordCreateCommand(TEST_USER_ID, code);
    }

    public static AlgorithmRecordUpdateCommand createAlgorithmRecordUpdateCommand(String code) {
        return AlgorithmRecordFixtures.createAlgorithmRecordUpdateCommand(TEST_USER_ID, code);
    }

    public static AlgorithmRecordResult createAlgorithmRecordResult(User user) {
        return AlgorithmRecordFixtures.createAlgorithmRecordResult(user.getId());
    }

    public static AlgorithmRecordFixtures.AlgorithmRecordFixture algorithmRecordFixture(User user) {
        return AlgorithmRecordFixtures.algorithmRecordFixture(user.getId());
    }

    public static AlgorithmRecordFixtures.AlgorithmRecordFixture algorithmRecordFixture(Long userId) {
        return AlgorithmRecordFixtures.algorithmRecordFixture(userId);
    }

    // Onboarding Fixtures
    public static RepositorySetupCommand createRepositorySetupCommand(Long userId) {
        return OnboardingFixtures.createRepositorySetupCommand(userId);
    }

    public static RepositorySetupResult createRepositorySetupResult(Long userId, boolean webhookConfigured) {
        return OnboardingFixtures.createRepositorySetupResult(userId, webhookConfigured);
    }

    public static Onboarding createOnboarding(Long userId, boolean webhookConfigured) {
        return OnboardingFixtures.createOnboarding(userId, webhookConfigured);
    }

    public static OnboardingFixtures.OnboardingFixture onboardingFixture(Long userId, boolean webhookConfigured) {
        return OnboardingFixtures.onboardingFixture(userId, webhookConfigured);
    }

}
