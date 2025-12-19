package com.ssafy.dash.user.application.dto.result;

import com.ssafy.dash.user.domain.User;

import java.time.LocalDateTime;

public record UserResult(
        Long id,
        String username,
        String email,
        LocalDateTime createdAt,
        String provider,
        String providerId,

        String avatarUrl,
        Long studyId,
        String solvedacHandle,
        Integer solvedacTier,
        String repositoryName,
        Boolean webhookConfigured
) {

    public static UserResult from(User user) {
        return from(user, null);
    }

    public static UserResult from(User user, com.ssafy.dash.onboarding.domain.Onboarding onboarding) {
        String repoName = onboarding != null ? onboarding.getRepositoryName() : null;
        Boolean webhook = onboarding != null ? onboarding.isWebhookConfigured() : null;

        return new UserResult(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getProvider(),
                user.getProviderId(),
                user.getAvatarUrl(),
                user.getStudyId(),
                user.getSolvedacHandle(),
                user.getSolvedacTier(),
                repoName,
                webhook
        );
    }

}
