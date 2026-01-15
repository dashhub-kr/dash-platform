package com.ssafy.dash.oauth.application;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.common.fixtures.FixtureTime;
import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.oauth.domain.UserOAuthTokenRepository;
import com.ssafy.dash.oauth.domain.exception.OAuthAccessTokenUnavailableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuthTokenService 단위 테스트")
@Disabled("테스트 환경 설정 필요")
class OAuthTokenServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserOAuthTokenRepository tokenRepository;

    @InjectMocks
    private OAuthTokenService oauthTokenService;

    @Test
    @DisplayName("저장된 토큰이 없으면 새 토큰을 저장한다")
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
    @DisplayName("기존 토큰이 있으면 내용을 갱신한다")
    void saveAccessToken_updateWhenPresent() {
        UserOAuthToken existing = TestFixtures.userOAuthTokenFixture(USER_ID)
            .withAccessToken("old")
            .toDomain(FixtureTime.now().plusHours(1));
        when(tokenRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "new-value",
                Instant.now(),
                Instant.now().plusSeconds(3600));

        oauthTokenService.saveAccessToken(USER_ID, accessToken);

        ArgumentCaptor<UserOAuthToken> captor = ArgumentCaptor.forClass(UserOAuthToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(existing.getAccessToken()).isEqualTo("new-value");
    }

    @Test
    @DisplayName("입력이 null이면 저장을 건너뛴다")
    void saveAccessToken_nullInput() {
        oauthTokenService.saveAccessToken(null, null);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("만료된 토큰을 요구하면 OAuthAccessTokenUnavailableException이 발생한다")
    void requireValidAccessToken_expired() {
        UserOAuthToken expired = TestFixtures.userOAuthTokenFixture(USER_ID)
            .withIssuedAt(FixtureTime.now().minusDays(1))
            .toDomain(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByUserId(USER_ID)).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> oauthTokenService.requireValidAccessToken(USER_ID))
                .isInstanceOf(OAuthAccessTokenUnavailableException.class)
                .hasMessageContaining("만료");
    }

}
