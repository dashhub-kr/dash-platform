package com.ssafy.dash.onboarding.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.oauth.service.OAuthTokenService;
import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.onboarding.dto.RepositorySetupRequest;
import com.ssafy.dash.onboarding.dto.RepositorySetupResponse;
import com.ssafy.dash.onboarding.exception.WebhookRegistrationException;
import com.ssafy.dash.github.service.GitHubWebhookService;
import com.ssafy.dash.onboarding.mapper.OnboardingRepositoryMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingService 단위 테스트")
class OnboardingServiceTest {

    private static final Long USER_ID = 1L;
    private static final String REPOSITORY = "utact/dash";

    @Mock
    private OnboardingRepositoryMapper repositoryMapper;

    @Mock
    private GitHubWebhookService gitHubWebhookService;

    @Mock
    private OAuthTokenService oauthTokenService;

    @InjectMocks
    private OnboardingService onboardingService;

    @Test
    @DisplayName("저장소 최초 등록 시 웹훅 설정 성공")
    void setupRepository_firstTimeSuccess() {
        when(repositoryMapper.selectByUserId(USER_ID)).thenReturn(null);
        when(oauthTokenService.findByUserId(USER_ID)).thenReturn(createToken());
        doNothing().when(gitHubWebhookService).ensureWebhook(REPOSITORY, "access-token");

        RepositorySetupResponse response = onboardingService.setupRepository(USER_ID,
                new RepositorySetupRequest(REPOSITORY));

        ArgumentCaptor<OnboardingRepository> repositoryCaptor = ArgumentCaptor.forClass(OnboardingRepository.class);
        verify(repositoryMapper).insert(repositoryCaptor.capture());
        verify(repositoryMapper).update(repositoryCaptor.getValue());
        verify(gitHubWebhookService).ensureWebhook(REPOSITORY, "access-token");
        assertThat(repositoryCaptor.getValue().isWebhookConfigured()).isTrue();

        assertThat(response.isWebhookConfigured()).isTrue();
        assertThat(response.getRepositoryName()).isEqualTo(REPOSITORY);
    }

    @Test
    @DisplayName("웹훅 생성 실패 시 예외를 전파하고 상태를 false로 유지")
    void setupRepository_webhookFailure() {
        OnboardingRepository existing = new OnboardingRepository();
        existing.setId(10L);
        existing.setUserId(USER_ID);
        existing.setRepositoryName("old/repo");
        existing.setWebhookConfigured(true);

        when(repositoryMapper.selectByUserId(USER_ID)).thenReturn(existing);
        when(oauthTokenService.findByUserId(USER_ID)).thenReturn(createToken());
        doThrow(new WebhookRegistrationException("GitHub 오류"))
            .when(gitHubWebhookService).ensureWebhook(REPOSITORY, "access-token");

        assertThatThrownBy(() -> onboardingService.setupRepository(USER_ID,
                new RepositorySetupRequest(REPOSITORY)))
                .isInstanceOf(WebhookRegistrationException.class)
                .hasMessageContaining("GitHub 오류");

        verify(gitHubWebhookService).ensureWebhook(REPOSITORY, "access-token");
        verify(repositoryMapper, times(2)).update(eq(existing));
        assertThat(existing.isWebhookConfigured()).isFalse();
    }

    @Test
    @DisplayName("토큰이 없으면 웹훅 생성 전 예외 발생")
    void setupRepository_missingToken() {
        when(repositoryMapper.selectByUserId(USER_ID)).thenReturn(null);
        when(oauthTokenService.findByUserId(USER_ID)).thenReturn(null);

        assertThatThrownBy(() -> onboardingService.setupRepository(USER_ID,
                new RepositorySetupRequest(REPOSITORY)))
                .isInstanceOf(WebhookRegistrationException.class)
                .hasMessageContaining("액세스 토큰");

        verify(repositoryMapper).insert(any(OnboardingRepository.class));
        verify(gitHubWebhookService, times(0)).ensureWebhook(anyString(), anyString());
    }

    private UserOAuthToken createToken() {
        UserOAuthToken token = new UserOAuthToken();
        token.setUserId(USER_ID);
        token.setAccessToken("access-token");
        return token;
    }
    
}
