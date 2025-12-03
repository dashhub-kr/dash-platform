package com.ssafy.dash.oauth.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class UserOAuthToken {

    private Long userId;
    private String accessToken;
    private String tokenType;
    private LocalDateTime expiresAt;
    private String refreshToken;
    private LocalDateTime refreshTokenExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserOAuthToken issued(Long userId, String accessToken, String tokenType, LocalDateTime expiresAt, String refreshToken, LocalDateTime refreshTokenExpiresAt, LocalDateTime issuedAt) {
        UserOAuthToken token = new UserOAuthToken();
        token.setUserId(requirePositive(userId));
        token.updateAccessToken(accessToken, tokenType, expiresAt, issuedAt);
        token.setRefreshToken(refreshToken);
        token.setRefreshTokenExpiresAt(refreshTokenExpiresAt);
        token.setCreatedAt(issuedAt);
        token.setUpdatedAt(issuedAt);
        return token;
    }

    public void updateAccessToken(String accessToken, String tokenType, LocalDateTime expiresAt, LocalDateTime updatedAt) {
        this.accessToken = requireText(accessToken);
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
        this.updatedAt = Objects.requireNonNullElse(updatedAt, LocalDateTime.now());
    }

    public boolean isAccessTokenExpired(LocalDateTime referenceTime) {
        if (expiresAt == null) {
            return false;
        }
        return expiresAt.isBefore(Objects.requireNonNullElse(referenceTime, LocalDateTime.now()));
    }

    private static Long requirePositive(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("userId" + " must be positive");
        }
        return value;
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("accessToken" + " must not be blank");
        }
        return value;
    }

}
