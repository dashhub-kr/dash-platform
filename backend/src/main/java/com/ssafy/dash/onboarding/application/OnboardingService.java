package com.ssafy.dash.onboarding.application;

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
import com.ssafy.dash.study.application.StudyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;
    private final GitHubWebhookService gitHubWebhookService;
    private final OAuthTokenService oauthTokenService;
    private final StudyService studyService;

    public OnboardingService(OnboardingRepository onboardingRepository, GitHubWebhookService gitHubWebhookService,
            OAuthTokenService oauthTokenService, StudyService studyService) {
        this.onboardingRepository = onboardingRepository;
        this.gitHubWebhookService = gitHubWebhookService;
        this.oauthTokenService = oauthTokenService;
        this.studyService = studyService;
    }

    @Transactional
    public RepositorySetupResult setupRepository(RepositorySetupCommand command) {
        String repositoryName = command.repositoryName().trim();
        Long userId = command.userId();
        Onboarding repository = onboardingRepository.findByUserId(userId)
                .map(existing -> {
                    existing.updateRepository(repositoryName, LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> Onboarding.create(userId, repositoryName, LocalDateTime.now()));
        onboardingRepository.save(repository);

        UserOAuthToken oauthToken;
        try {
            oauthToken = oauthTokenService.requireValidAccessToken(userId);
        } catch (OAuthAccessTokenUnavailableException ex) {
            throw new WebhookRegistrationException(ex.getMessage(), ex);
        }

        try {
            gitHubWebhookService.ensureWebhook(repositoryName, oauthToken.getAccessToken());
            repository.markWebhookConfigured(true, LocalDateTime.now());
            onboardingRepository.save(repository);
        } catch (GitHubWebhookException ex) {
            repository.markWebhookConfigured(false, LocalDateTime.now());
            onboardingRepository.save(repository);
            throw new WebhookRegistrationException(ex.getMessage(), ex);
        }

        // Auto-create Personal Study (Personal Lab) for the user
        studyService.createPersonalStudy(userId);

        return new RepositorySetupResult(userId, repository.getRepositoryName(), repository.isWebhookConfigured());
    }

}
