package com.ssafy.dash.common.fixtures;

import java.time.LocalDateTime;

import com.ssafy.dash.user.application.dto.command.UserCreateCommand;
import com.ssafy.dash.user.application.dto.command.UserUpdateCommand;
import com.ssafy.dash.user.application.dto.result.UserResult;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.presentation.dto.request.UserCreateRequest;
import com.ssafy.dash.user.presentation.dto.request.UserUpdateRequest;

public final class UserFixtures {

    public static final Long TEST_USER_ID = 1L;
    public static final String TEST_USERNAME = "tester";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PROVIDER = "github";
    public static final String TEST_PROVIDER_ID = "12345678";
    public static final String TEST_AVATAR_URL = "http://avatar.url";
    public static final String UPDATED_USERNAME = "Updated User";
    public static final String UPDATED_EMAIL = "updated@example.com";

    private UserFixtures() {
    }

    public static User createUser() {
        return userFixture().toDomain(FixtureTime.now());
    }

    public static UserCreateRequest createUserCreateRequest() {
        return userFixture().toCreateRequest();
    }

    public static UserUpdateRequest createUserUpdateRequest() {
        return new UserUpdateRequest(UPDATED_USERNAME, UPDATED_EMAIL);
    }

    public static UserCreateCommand createUserCreateCommand() {
        return userFixture().toCreateCommand();
    }

    public static UserUpdateCommand createUserUpdateCommand() {
        return new UserUpdateCommand(UPDATED_USERNAME, UPDATED_EMAIL);
    }

    public static UserResult createUserResult() {
        return userFixture().toResult(FixtureTime.now());
    }

    public static UserFixture userFixture() {
        return new UserFixture(TEST_USER_ID, TEST_USERNAME, TEST_EMAIL, TEST_PROVIDER,
                TEST_PROVIDER_ID, TEST_AVATAR_URL);
    }

    public record UserFixture(
            Long id,
            String username,
            String email,
            String provider,
            String providerId,
            String avatarUrl) {

        public User toDomain(LocalDateTime createdAt) {
            User user = User.create(username, email, createdAt, provider, providerId, avatarUrl);
            user.setId(id);
            return user;
        }

        public UserResult toResult(LocalDateTime createdAt) {
            return new UserResult(id, username, email, createdAt, provider, providerId, avatarUrl, null, null, null,
                    null, null, null, null, null, null);
        }

        public UserCreateCommand toCreateCommand() {
            return new UserCreateCommand(username, email);
        }

        public UserCreateRequest toCreateRequest() {
            return new UserCreateRequest(username, email);
        }

    }

}
