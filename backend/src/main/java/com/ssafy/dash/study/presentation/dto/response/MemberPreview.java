package com.ssafy.dash.study.presentation.dto.response;

/**
 * 스터디 카드용 멤버 미리보기 DTO (아바타 표시용)
 */
public record MemberPreview(
        Long userId,
        String username,
        String avatarUrl) {
    public static MemberPreview from(com.ssafy.dash.user.domain.User user) {
        return new MemberPreview(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl());
    }
}
