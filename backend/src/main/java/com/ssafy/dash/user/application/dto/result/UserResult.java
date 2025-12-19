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
        Long studyId
) {

    public static UserResult from(User user) {
        return new UserResult(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getProvider(),
                user.getProviderId(),
                user.getAvatarUrl(),
                user.getStudyId()
        );
    }

}
