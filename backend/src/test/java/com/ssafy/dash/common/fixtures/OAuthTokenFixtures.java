package com.ssafy.dash.common.fixtures;

import java.time.LocalDateTime;

import com.ssafy.dash.oauth.domain.UserOAuthToken;

public final class OAuthTokenFixtures {

    public static final String TEST_ACCESS_TOKEN = "access-token";
    public static final String UPDATED_ACCESS_TOKEN = "updated-access-token";
    public static final String TEST_REFRESH_TOKEN = "refresh-token";
    public static final LocalDateTime TEST_REFRESH_TOKEN_EXPIRES_AT = FixtureTime.now().plusDays(30);
    public static final String TEST_TOKEN_TYPE = "Bearer";

    private OAuthTokenFixtures() {
    }

    public static UserOAuthTokenFixture userOAuthTokenFixture(Long userId) {
        return new UserOAuthTokenFixture(
            userId,
            TEST_ACCESS_TOKEN,
            TEST_TOKEN_TYPE,
            TEST_REFRESH_TOKEN,
            TEST_REFRESH_TOKEN_EXPIRES_AT,
            FixtureTime.now()
        );
    }

    public record UserOAuthTokenFixture(
        Long userId,
        String accessToken,
        String tokenType,
        String refreshToken,
        LocalDateTime refreshTokenExpiresAt,
        LocalDateTime issuedAt
    ) {

        public UserOAuthToken toDomain(LocalDateTime expiresAt) {
            return UserOAuthToken.issued(userId, accessToken, tokenType, expiresAt, refreshToken, refreshTokenExpiresAt, issuedAt);
        }

        public UserOAuthToken toDomain() {
            return toDomain(issuedAt.plusHours(1));
        }

        public UserOAuthToken expiredToken() {
            return toDomain(issuedAt.minusMinutes(5));
        }

        public UserOAuthTokenFixture withAccessToken(String newAccessToken) {
            return new UserOAuthTokenFixture(userId, newAccessToken, tokenType, refreshToken, refreshTokenExpiresAt, issuedAt);
        }

        public UserOAuthTokenFixture withTokenType(String newTokenType) {
            return new UserOAuthTokenFixture(userId, accessToken, newTokenType, refreshToken, refreshTokenExpiresAt, issuedAt);
        }

        public UserOAuthTokenFixture withRefreshToken(String newRefreshToken) {
            return new UserOAuthTokenFixture(userId, accessToken, tokenType, newRefreshToken, refreshTokenExpiresAt, issuedAt);
        }

        public UserOAuthTokenFixture withRefreshTokenExpiresAt(LocalDateTime newExpiresAt) {
            return new UserOAuthTokenFixture(userId, accessToken, tokenType, refreshToken, newExpiresAt, issuedAt);
        }

        public UserOAuthTokenFixture withIssuedAt(LocalDateTime newIssuedAt) {
            return new UserOAuthTokenFixture(userId, accessToken, tokenType, refreshToken, refreshTokenExpiresAt, newIssuedAt);
        }

    }

}
