package com.ssafy.dash.onboarding.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ssafy.dash.github.application.GitHubWebhookService;
import com.ssafy.dash.github.domain.exception.GitHubWebhookException;
import com.ssafy.dash.oauth.application.OAuthTokenService;
import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.onboarding.application.dto.RepositorySetupCommand;
import com.ssafy.dash.onboarding.application.dto.RepositorySetupResult;
import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.onboarding.domain.OnboardingRepositoryPort;
import com.ssafy.dash.onboarding.domain.exception.WebhookRegistrationException;

@Service
public class OnboardingService {

    private final OnboardingRepositoryPort repositoryPort;
    private final GitHubWebhookService gitHubWebhookService;
    private final OAuthTokenService oauthTokenService;

    public OnboardingService(OnboardingRepositoryPort repositoryPort,
            GitHubWebhookService gitHubWebhookService,
            OAuthTokenService oauthTokenService) {
        this.repositoryPort = repositoryPort;
        this.gitHubWebhookService = gitHubWebhookService;
        this.oauthTokenService = oauthTokenService;
    }

    @Transactional
    public RepositorySetupResult setupRepository(RepositorySetupCommand command) {
        String repositoryName = command.getRepositoryName().trim();
        Long userId = command.getUserId();
        OnboardingRepository repository = repositoryPort.findByUserId(userId).orElse(null);
        
        if (repository == null) {
            repository = new OnboardingRepository();
            repository.setUserId(userId);
            repository.setRepositoryName(repositoryName);
            repository.setWebhookConfigured(false);
            repositoryPort.save(repository);
        } else {
            repository.setRepositoryName(repositoryName);
            repository.setWebhookConfigured(false);
            repositoryPort.save(repository);
        }

        UserOAuthToken oauthToken = oauthTokenService.findByUserId(userId);
        if (oauthToken == null || !StringUtils.hasText(oauthToken.getAccessToken())) {
            
            throw new WebhookRegistrationException("GitHub 액세스 토큰이 존재하지 않습니다. 다시 로그인해주세요.");
        }

        try {
            gitHubWebhookService.ensureWebhook(repositoryName, oauthToken.getAccessToken());
            repository.setWebhookConfigured(true);
            repositoryPort.save(repository);
        } catch (GitHubWebhookException ex) {
            repositoryPort.save(repository);

            throw new WebhookRegistrationException(ex.getMessage(), ex);
        }

        return new RepositorySetupResult(userId, repository.getRepositoryName(), repository.isWebhookConfigured());
    }
    
}
