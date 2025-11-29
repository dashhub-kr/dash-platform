package com.ssafy.dash.onboarding.application;

import java.util.Optional;

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

import com.ssafy.dash.github.application.GitHubWebhookService;
import com.ssafy.dash.github.domain.exception.GitHubWebhookException;
import com.ssafy.dash.oauth.application.OAuthTokenService;
import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.onboarding.application.dto.RepositorySetupCommand;
import com.ssafy.dash.onboarding.application.dto.RepositorySetupResult;
import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.onboarding.domain.OnboardingRepositoryPort;
import com.ssafy.dash.onboarding.domain.exception.WebhookRegistrationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingService 단위 테스트")
class OnboardingServiceTest {

    private static final Long USER_ID = 1L;
    private static final String REPOSITORY = "utact/dash";

    @Mock
    private OnboardingRepositoryPort repositoryPort;

    @Mock
    private GitHubWebhookService gitHubWebhookService;

    @Mock
    private OAuthTokenService oauthTokenService;

    @InjectMocks
    private OnboardingService onboardingService;

    @Test
    @DisplayName("저장소 최초 등록 시 웹훅 설정 성공")
    void setupRepository_firstTimeSuccess() {
        when(repositoryPort.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(oauthTokenService.findByUserId(USER_ID)).thenReturn(createToken());
        doNothing().when(gitHubWebhookService).ensureWebhook(REPOSITORY, "access-token");

        RepositorySetupResult result = onboardingService.setupRepository(new RepositorySetupCommand(USER_ID, REPOSITORY));

        ArgumentCaptor<OnboardingRepository> repositoryCaptor = ArgumentCaptor.forClass(OnboardingRepository.class);
        // 저장은 두 번 호출됨: 한 번은 최초 생성 시, 한 번은 웹훅 설정 후
        verify(repositoryPort, times(2)).save(repositoryCaptor.capture());
        verify(gitHubWebhookService).ensureWebhook(REPOSITORY, "access-token");
        
        // 최근 저장이 웹훅 설정 후의 상태임
        assertThat(repositoryCaptor.getAllValues().get(1).isWebhookConfigured()).isTrue();

        assertThat(result.isWebhookConfigured()).isTrue();
        assertThat(result.getRepositoryName()).isEqualTo(REPOSITORY);
    }

    @Test
    @DisplayName("웹훅 생성 실패 시 예외를 전파하고 상태를 false로 유지")
    void setupRepository_webhookFailure() {
        OnboardingRepository existing = new OnboardingRepository();
        existing.setId(10L);
        existing.setUserId(USER_ID);
        existing.setRepositoryName("old/repo");
        existing.setWebhookConfigured(true);

        when(repositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
        when(oauthTokenService.findByUserId(USER_ID)).thenReturn(createToken());
        doThrow(new GitHubWebhookException("GitHub 오류"))
            .when(gitHubWebhookService).ensureWebhook(REPOSITORY, "access-token");

        assertThatThrownBy(() -> onboardingService.setupRepository(new RepositorySetupCommand(USER_ID, REPOSITORY)))
                .isInstanceOf(WebhookRegistrationException.class)
                .hasMessageContaining("GitHub 오류");

        verify(gitHubWebhookService).ensureWebhook(REPOSITORY, "access-token");
        // 저장 2번 호출 -> 이름/웹훅 재설정 시, 예외 처리 블록
        verify(repositoryPort, times(2)).save(eq(existing));
        
        // 웹훅 호출 전에 리셋된 상태 유지 확인
        assertThat(existing.isWebhookConfigured()).isFalse();
    }

    @Test
    @DisplayName("토큰이 없으면 웹훅 생성 전 예외 발생")
    void setupRepository_missingToken() {
        when(repositoryPort.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(oauthTokenService.findByUserId(USER_ID)).thenReturn(null);

        assertThatThrownBy(() -> onboardingService.setupRepository(new RepositorySetupCommand(USER_ID, REPOSITORY)))
                .isInstanceOf(WebhookRegistrationException.class)
                .hasMessageContaining("액세스 토큰");

        verify(repositoryPort).save(any(OnboardingRepository.class));
        verify(gitHubWebhookService, times(0)).ensureWebhook(anyString(), anyString());
    }

    private UserOAuthToken createToken() {
        UserOAuthToken token = new UserOAuthToken();
        token.setUserId(USER_ID);
        token.setAccessToken("access-token");

        return token;
    }
    
}
