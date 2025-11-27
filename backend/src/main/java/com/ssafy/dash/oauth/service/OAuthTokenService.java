package com.ssafy.dash.oauth.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.oauth.mapper.UserOAuthTokenMapper;

@Service
public class OAuthTokenService {

    private final UserOAuthTokenMapper tokenMapper;

    public OAuthTokenService(UserOAuthTokenMapper tokenMapper) {
        this.tokenMapper = tokenMapper;
    }

    @Transactional
    public void saveAccessToken(Long userId, OAuth2AccessToken accessToken) {
        if (userId == null || accessToken == null) {
            return;
        }

        UserOAuthToken token = tokenMapper.selectByUserId(userId);
        if (token == null) {
            token = new UserOAuthToken();
            token.setUserId(userId);
            applyAccessToken(token, accessToken);
            tokenMapper.insert(token);
        } else {
            applyAccessToken(token, accessToken);
            tokenMapper.update(token);
        }
    }

    @Transactional(readOnly = true)
    public UserOAuthToken findByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        return tokenMapper.selectByUserId(userId);
    }

    private void applyAccessToken(UserOAuthToken token, OAuth2AccessToken accessToken) {
        token.setAccessToken(accessToken.getTokenValue());
        token.setTokenType(accessToken.getTokenType() != null ? accessToken.getTokenType().getValue() : null);
        token.setExpiresAt(toLocalDateTime(accessToken.getExpiresAt()));
        token.setRefreshToken(null);
        token.setRefreshTokenExpiresAt(null);
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

}
