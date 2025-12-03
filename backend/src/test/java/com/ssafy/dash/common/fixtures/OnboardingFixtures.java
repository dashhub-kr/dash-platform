package com.ssafy.dash.common.fixtures;

import java.time.LocalDateTime;

import com.ssafy.dash.onboarding.application.dto.command.RepositorySetupCommand;
import com.ssafy.dash.onboarding.application.dto.result.RepositorySetupResult;
import com.ssafy.dash.onboarding.domain.Onboarding;

public final class OnboardingFixtures {

    public static final Long TEST_ONBOARDING_ID = 1L;
    public static final String TEST_REPOSITORY_NAME = "utact/dash";

    private OnboardingFixtures() {
    }

    public static RepositorySetupCommand createRepositorySetupCommand(Long userId) {
        return onboardingFixture(userId, false).toCommand();
    }

    public static RepositorySetupResult createRepositorySetupResult(Long userId, boolean webhookConfigured) {
        return onboardingFixture(userId, webhookConfigured).toResult();
    }

    public static Onboarding createOnboarding(Long userId, boolean webhookConfigured) {
        return onboardingFixture(userId, webhookConfigured).toDomain();
    }

    public static OnboardingFixture onboardingFixture(Long userId, boolean webhookConfigured) {
        return new OnboardingFixture(TEST_ONBOARDING_ID, userId, TEST_REPOSITORY_NAME, webhookConfigured);
    }

    public record OnboardingFixture(
            Long id,
            Long userId,
            String repositoryName,
            boolean webhookConfigured
    ) {

        public Onboarding toDomain(LocalDateTime createdAt, LocalDateTime updatedAt) {
            Onboarding onboarding = Onboarding.create(userId, repositoryName, createdAt);
            onboarding.setId(id);
            onboarding.markWebhookConfigured(webhookConfigured, updatedAt);
            return onboarding;
        }

        public Onboarding toDomain() {
            LocalDateTime now = FixtureTime.now();
            return toDomain(now, now);
        }

        public RepositorySetupCommand toCommand() {
            return new RepositorySetupCommand(userId, repositoryName);
        }

        public RepositorySetupResult toResult() {
            return new RepositorySetupResult(userId, repositoryName, webhookConfigured);
        }

    }
    
}
