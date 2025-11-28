package com.ssafy.dash.oauth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.oauth.domain.UserOAuthTokenRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuthTokenService 단위 테스트")
class OAuthTokenServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserOAuthTokenRepository tokenRepository;

    @InjectMocks
    private OAuthTokenService oauthTokenService;

    @Test
    @DisplayName("토큰이 없는 경우 신규 저장 성공")
    void saveAccessToken_insertWhenAbsent() {
        when(tokenRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-value",
                Instant.now(),
                Instant.now().plusSeconds(3600));

        oauthTokenService.saveAccessToken(USER_ID, accessToken);

        ArgumentCaptor<UserOAuthToken> captor = ArgumentCaptor.forClass(UserOAuthToken.class);
        verify(tokenRepository).save(captor.capture());
        UserOAuthToken saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getAccessToken()).isEqualTo("access-value");
        assertThat(saved.getTokenType()).isEqualTo("Bearer");
        assertThat(saved.getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("토큰이 있는 경우 업데이트 성공")
    void saveAccessToken_updateWhenPresent() {
        UserOAuthToken existing = new UserOAuthToken();
        existing.setUserId(USER_ID);
        existing.setAccessToken("old");
        when(tokenRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "new-value",
                Instant.now(),
                Instant.now().plusSeconds(3600));

        oauthTokenService.saveAccessToken(USER_ID, accessToken);

        ArgumentCaptor<UserOAuthToken> captor = ArgumentCaptor.forClass(UserOAuthToken.class);
        verify(tokenRepository).save(captor.capture());
        UserOAuthToken updated = captor.getValue();
        assertThat(updated.getAccessToken()).isEqualTo("new-value");
    }

    @Test
    @DisplayName("null 인자가 들어오는 경우 저장하지 않음")
    void saveAccessToken_nullInput() {
        oauthTokenService.saveAccessToken(null, null);
        verify(tokenRepository, never()).save(any());
    }

}
