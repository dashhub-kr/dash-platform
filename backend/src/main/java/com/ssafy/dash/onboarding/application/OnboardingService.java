package com.ssafy.dash.onboarding.application;

import com.ssafy.dash.onboarding.application.dto.command.RepositorySetupCommand;
import com.ssafy.dash.onboarding.application.dto.result.RepositorySetupResult;
import com.ssafy.dash.onboarding.domain.Onboarding;
import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.onboarding.domain.exception.GitHubAppNotInstalledException;
import com.ssafy.dash.github.application.GitHubAppService;
import com.ssafy.dash.study.application.StudyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;
    private final StudyService studyService;
    private final GitHubAppService gitHubAppService;

    public OnboardingService(OnboardingRepository onboardingRepository, StudyService studyService,
            GitHubAppService gitHubAppService) {
        this.onboardingRepository = onboardingRepository;
        this.studyService = studyService;
        this.gitHubAppService = gitHubAppService;
    }

    @Transactional
    public RepositorySetupResult setupRepository(RepositorySetupCommand command) {
        String repositoryName = command.repositoryName().trim();

        // GitHub App 설치 여부 사전 검증
        if (!gitHubAppService.isAppInstalledOnRepo(repositoryName)) {
            throw new GitHubAppNotInstalledException(repositoryName);
        }

        Long userId = command.userId();
        Onboarding repository = onboardingRepository.findByUserId(userId)
                .map(existing -> {
                    existing.updateRepository(repositoryName, LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> Onboarding.create(userId, repositoryName, LocalDateTime.now()));
        onboardingRepository.save(repository);

        repository.markWebhookConfigured(true, LocalDateTime.now());
        onboardingRepository.save(repository);

        // Auto-create Personal Study (Personal Lab) for the user
        studyService.createPersonalStudy(userId);

        return new RepositorySetupResult(userId, repository.getRepositoryName(), repository.isWebhookConfigured());
    }

}
