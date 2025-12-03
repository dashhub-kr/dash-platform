package com.ssafy.dash.onboarding.application;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.github.application.GitHubWebhookService;
import com.ssafy.dash.github.domain.exception.GitHubWebhookException;
import com.ssafy.dash.oauth.application.OAuthTokenService;
import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.oauth.domain.exception.OAuthAccessTokenUnavailableException;
import com.ssafy.dash.onboarding.application.dto.command.RepositorySetupCommand;
import com.ssafy.dash.onboarding.application.dto.result.RepositorySetupResult;
import com.ssafy.dash.onboarding.domain.Onboarding;
import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.onboarding.domain.exception.WebhookRegistrationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingService 단위 테스트")
class OnboardingServiceTest {

    private static final Long USER_ID = TestFixtures.TEST_USER_ID;
    private static final String REPOSITORY = TestFixtures.TEST_REPOSITORY_NAME;

    @Mock
    private OnboardingRepository onboardingRepository;

    @Mock
    private GitHubWebhookService gitHubWebhookService;

    @Mock
    private OAuthTokenService oauthTokenService;

    @InjectMocks
    private OnboardingService onboardingService;

    @Test
    @DisplayName("처음 저장소를 등록하면 웹훅을 설정하고 true를 반환한다")
    void setupRepository_firstTimeSuccess() {
        when(onboardingRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(oauthTokenService.requireValidAccessToken(USER_ID)).thenReturn(createToken());
        doNothing().when(gitHubWebhookService).ensureWebhook(REPOSITORY, TestFixtures.TEST_ACCESS_TOKEN);

        RepositorySetupResult result = onboardingService.setupRepository(new RepositorySetupCommand(USER_ID, REPOSITORY));

        ArgumentCaptor<Onboarding> repositoryCaptor = ArgumentCaptor.forClass(Onboarding.class);
        // 저장은 두 번 호출됨: 한 번은 최초 생성 시, 한 번은 웹훅 설정 후
        verify(onboardingRepository, times(2)).save(repositoryCaptor.capture());
        verify(gitHubWebhookService).ensureWebhook(REPOSITORY, TestFixtures.TEST_ACCESS_TOKEN);
        
        // 최근 저장이 웹훅 설정 후의 상태임
        assertThat(repositoryCaptor.getAllValues().get(1).isWebhookConfigured()).isTrue();

        assertThat(result.webhookConfigured()).isTrue();
        assertThat(result.repositoryName()).isEqualTo(REPOSITORY);
    }

    @Test
    @DisplayName("웹훅 생성이 실패하면 WebhookRegistrationException을 던지고 상태를 false로 유지한다")
    void setupRepository_webhookFailure() {
        Onboarding existing = TestFixtures.onboardingFixture(USER_ID, true).toDomain();
        existing.setRepositoryName("old/repo");

        when(onboardingRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
        when(oauthTokenService.requireValidAccessToken(USER_ID)).thenReturn(createToken());
        doThrow(new GitHubWebhookException("GitHub 오류"))
            .when(gitHubWebhookService).ensureWebhook(REPOSITORY, TestFixtures.TEST_ACCESS_TOKEN);

        assertThatThrownBy(() -> onboardingService.setupRepository(new RepositorySetupCommand(USER_ID, REPOSITORY)))
                .isInstanceOf(WebhookRegistrationException.class)
                .hasMessageContaining("GitHub 오류");

        verify(gitHubWebhookService).ensureWebhook(REPOSITORY, TestFixtures.TEST_ACCESS_TOKEN);
        // 저장 2번 호출 -> 이름/웹훅 재설정 시, 예외 처리 블록
        verify(onboardingRepository, times(2)).save(eq(existing));
        
        // 웹훅 호출 전에 리셋된 상태 유지 확인
        assertThat(existing.isWebhookConfigured()).isFalse();
    }

    @Test
    @DisplayName("유효한 토큰이 없으면 WebhookRegistrationException으로 wrapping된 OAuthAccessTokenUnavailableException이 발생한다")
    void setupRepository_missingToken() {
        when(onboardingRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(oauthTokenService.requireValidAccessToken(USER_ID)).thenThrow(new OAuthAccessTokenUnavailableException("토큰 없음"));

        assertThatThrownBy(() -> onboardingService.setupRepository(new RepositorySetupCommand(USER_ID, REPOSITORY)))
            .isInstanceOf(WebhookRegistrationException.class)
            .hasMessageContaining("토큰 없음");

        verify(onboardingRepository).save(any(Onboarding.class));
        verify(gitHubWebhookService, times(0)).ensureWebhook(eq(REPOSITORY), eq(TestFixtures.TEST_ACCESS_TOKEN));
    }

    private UserOAuthToken createToken() {
        return TestFixtures.userOAuthTokenFixture(USER_ID).toDomain();
    }
    
}
