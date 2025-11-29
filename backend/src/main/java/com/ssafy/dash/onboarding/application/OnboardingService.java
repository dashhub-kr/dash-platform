package com.ssafy.dash.onboarding.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ssafy.dash.github.application.GitHubWebhookService;
import com.ssafy.dash.github.domain.exception.GitHubWebhookException;
import com.ssafy.dash.oauth.application.OAuthTokenService;
import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.onboarding.application.dto.RepositorySetupRequest;
import com.ssafy.dash.onboarding.application.dto.RepositorySetupResponse;
import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.onboarding.domain.OnboardingRepositoryRepository;
import com.ssafy.dash.onboarding.domain.exception.WebhookRegistrationException;

@Service
public class OnboardingService {

    private final OnboardingRepositoryRepository repositoryRepository;
    private final GitHubWebhookService gitHubWebhookService;
    private final OAuthTokenService oauthTokenService;

    public OnboardingService(OnboardingRepositoryRepository repositoryRepository,
            GitHubWebhookService gitHubWebhookService,
            OAuthTokenService oauthTokenService) {
        this.repositoryRepository = repositoryRepository;
        this.gitHubWebhookService = gitHubWebhookService;
        this.oauthTokenService = oauthTokenService;
    }

    @Transactional
    public RepositorySetupResponse setupRepository(Long userId, RepositorySetupRequest request) {
        String repositoryName = request.getRepositoryName().trim();
        OnboardingRepository repository = repositoryRepository.findByUserId(userId).orElse(null);
        
        if (repository == null) {
            repository = new OnboardingRepository();
            repository.setUserId(userId);
            repository.setRepositoryName(repositoryName);
            repository.setWebhookConfigured(false);
            repositoryRepository.save(repository);
        } else {
            repository.setRepositoryName(repositoryName);
            repository.setWebhookConfigured(false);
            repositoryRepository.save(repository);
        }

        UserOAuthToken oauthToken = oauthTokenService.findByUserId(userId);
        if (oauthToken == null || !StringUtils.hasText(oauthToken.getAccessToken())) {
            
            throw new WebhookRegistrationException("GitHub 액세스 토큰이 존재하지 않습니다. 다시 로그인해주세요.");
        }

        try {
            gitHubWebhookService.ensureWebhook(repositoryName, oauthToken.getAccessToken());
            repository.setWebhookConfigured(true);
            repositoryRepository.save(repository);
        } catch (GitHubWebhookException ex) {
            repositoryRepository.save(repository);

            throw new WebhookRegistrationException(ex.getMessage(), ex);
        }

        return new RepositorySetupResponse(userId, repository.getRepositoryName(), repository.isWebhookConfigured());
    }
    
}
