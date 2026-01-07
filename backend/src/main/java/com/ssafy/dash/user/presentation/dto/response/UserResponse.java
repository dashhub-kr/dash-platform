package com.ssafy.dash.user.presentation.dto.response;

import com.ssafy.dash.user.application.dto.result.UserResult;

import java.time.LocalDateTime;

public record UserResponse(
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
        String friendshipStatus,
        String equippedDecorationClass) {

    public static UserResponse from(UserResult result) {
        return new UserResponse(
                result.id(),
                result.username(),
                result.email(),
                result.createdAt(),
                result.provider(),
                result.providerId(),
                result.avatarUrl(),
                result.studyId(),
                result.solvedacHandle(),
                result.solvedacTier(),
                result.solvedCount(),
                result.repositoryName(),
                result.webhookConfigured(),
                result.isStudyLeader(),
                result.studyType(),
                result.role(),
                result.pendingStudyName(),
                result.friendshipStatus(),
                result.equippedDecorationClass());
    }

}
