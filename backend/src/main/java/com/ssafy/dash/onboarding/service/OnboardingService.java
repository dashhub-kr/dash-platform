package com.ssafy.dash.onboarding.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;

import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.oauth.service.OAuthTokenService;
import com.ssafy.dash.onboarding.dto.RepositorySetupRequest;
import com.ssafy.dash.onboarding.dto.RepositorySetupResponse;
import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.onboarding.exception.WebhookRegistrationException;
import com.ssafy.dash.github.service.GitHubWebhookService;
import com.ssafy.dash.onboarding.mapper.OnboardingRepositoryMapper;

@Service
public class OnboardingService {

    private final OnboardingRepositoryMapper repositoryMapper;
    private final GitHubWebhookService gitHubWebhookService;
    private final OAuthTokenService oauthTokenService;

    public OnboardingService(OnboardingRepositoryMapper repositoryMapper,
            GitHubWebhookService gitHubWebhookService,
            OAuthTokenService oauthTokenService) {
        this.repositoryMapper = repositoryMapper;
        this.gitHubWebhookService = gitHubWebhookService;
        this.oauthTokenService = oauthTokenService;
    }

    @Transactional
    public RepositorySetupResponse setupRepository(Long userId, RepositorySetupRequest request) {
        String repositoryName = request.getRepositoryName().trim();
        OnboardingRepository repository = repositoryMapper.selectByUserId(userId);
        if (repository == null) {
            repository = new OnboardingRepository();
            repository.setUserId(userId);
            repository.setRepositoryName(repositoryName);
            repository.setWebhookConfigured(false);
            repositoryMapper.insert(repository);
        } else {
            repository.setRepositoryName(repositoryName);
            repository.setWebhookConfigured(false);
            repositoryMapper.update(repository);
        }

        UserOAuthToken oauthToken = oauthTokenService.findByUserId(userId);
        if (oauthToken == null || !StringUtils.hasText(oauthToken.getAccessToken())) {
            throw new WebhookRegistrationException("GitHub 액세스 토큰이 존재하지 않습니다. 다시 로그인해주세요.");
        }

        try {
            gitHubWebhookService.ensureWebhook(repositoryName, oauthToken.getAccessToken());
            repository.setWebhookConfigured(true);
            repositoryMapper.update(repository);
        } catch (WebhookRegistrationException ex) {
            repositoryMapper.update(repository);
            
            throw ex;
        }

        return new RepositorySetupResponse(userId, repository.getRepositoryName(), repository.isWebhookConfigured());
    }
    
}
