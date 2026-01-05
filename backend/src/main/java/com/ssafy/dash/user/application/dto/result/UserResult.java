package com.ssafy.dash.user.application.dto.result;

import com.ssafy.dash.study.domain.Study;
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
        Integer solvedCount,
        String repositoryName,
        Boolean webhookConfigured,
        Boolean isStudyLeader,
        String studyType,
        String role,
        String pendingStudyName,
        String friendshipStatus) {

    public static UserResult from(User user) {
        return from(user, null, null, null);
    }

    public static UserResult from(User user, com.ssafy.dash.onboarding.domain.Onboarding onboarding) {
        return from(user, onboarding, null, null);
    }

    public static UserResult from(User user, com.ssafy.dash.onboarding.domain.Onboarding onboarding, Study study) {
        return from(user, onboarding, study, null);
    }

    public static UserResult from(User user, com.ssafy.dash.onboarding.domain.Onboarding onboarding, Study study,
            String pendingStudyName) {
        String repoName = onboarding != null ? onboarding.getRepositoryName() : null;
        Boolean webhook = onboarding != null ? onboarding.isWebhookConfigured() : null;
        Boolean isLeader = study != null && study.getCreatorId() != null && study.getCreatorId().equals(user.getId());
        String type = study != null && study.getStudyType() != null ? study.getStudyType().name() : null;

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
                user.getSolvedCount(),
                repoName,
                webhook,
                isLeader,
                type,
                user.getRole(),
                pendingStudyName,
                null);
    }

    public static UserResult from(User user, String friendshipStatus) {
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
                user.getSolvedCount(),
                null,
                null,
                null,
                null,
                user.getRole(),
                null,
                friendshipStatus);
    }

}
