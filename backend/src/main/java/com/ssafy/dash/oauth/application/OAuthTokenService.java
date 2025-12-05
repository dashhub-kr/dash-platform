package com.ssafy.dash.oauth.application;

import java.time.Instant;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.oauth.domain.UserOAuthTokenRepository;
import com.ssafy.dash.oauth.domain.exception.OAuthAccessTokenUnavailableException;

@Service
public class OAuthTokenService {

    private static final Logger log = LoggerFactory.getLogger(OAuthTokenService.class);

    private final UserOAuthTokenRepository tokenRepository;

    public OAuthTokenService(UserOAuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public void saveAccessToken(Long userId, OAuth2AccessToken accessToken) {
        if (userId == null || accessToken == null) {
            log.debug("saveAccessToken called with null userId or accessToken (userId={}, accessTokenPresent={})", userId, accessToken != null);
            return;
        }

        String tokenValue = accessToken.getTokenValue();
        String masked = mask(tokenValue);
        Instant issuedAtInstant = accessToken.getIssuedAt();
        Instant expiresAtInstant = accessToken.getExpiresAt();
        log.info("Persisting OAuth token for userId={} tokenMasked={} issuedAtUTC={} expiresAtUTC={} diffSeconds={}",
            userId,
            masked,
            issuedAtInstant,
            expiresAtInstant,
            (issuedAtInstant != null && expiresAtInstant != null) ? expiresAtInstant.getEpochSecond() - issuedAtInstant.getEpochSecond() : null);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = null;
        String tokenType = accessToken.getTokenType() != null ? accessToken.getTokenType().getValue() : null;

        UserOAuthToken token = tokenRepository.findByUserId(userId)
                .map(existing -> {
                    log.debug("Updating existing UserOAuthToken for userId={}", userId);
                    existing.updateAccessToken(tokenValue, tokenType, expiresAt, now);
                    return existing;
                })
                .orElseGet(() -> {
                    log.debug("Creating new UserOAuthToken for userId={}", userId);
                    return UserOAuthToken.issued(userId, tokenValue, tokenType, expiresAt, null, null, now);
                });

        tokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public UserOAuthToken findByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        return tokenRepository.findByUserId(userId).orElse(null);
    }

    @Transactional(readOnly = true)
    public UserOAuthToken requireValidAccessToken(Long userId) {
        if (userId == null) {
            throw new OAuthAccessTokenUnavailableException("사용자 정보가 없어서 GitHub 액세스 토큰을 확인할 수 없습니다.");
        }

        UserOAuthToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new OAuthAccessTokenUnavailableException("GitHub 액세스 토큰이 존재하지 않습니다. 다시 로그인해주세요."));

        if (!StringUtils.hasText(token.getAccessToken())) {
            throw new OAuthAccessTokenUnavailableException("GitHub 액세스 토큰이 비어 있습니다. 다시 로그인해주세요.");
        }

        if (token.isAccessTokenExpired(LocalDateTime.now())) {
            throw new OAuthAccessTokenUnavailableException("GitHub 액세스 토큰이 만료되었습니다. 다시 로그인해주세요.");
        }

        return token;
    }

    private String mask(String tokenValue) {
        if (!StringUtils.hasText(tokenValue)) {
            return "null";
        }
        
        return tokenValue.length() > 8 ? tokenValue.substring(0, 6) + "..." : "***";
    }

}
