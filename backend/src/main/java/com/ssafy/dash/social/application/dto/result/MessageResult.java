package com.ssafy.dash.social.application.dto.result;

import com.ssafy.dash.social.domain.DirectMessage;

import com.ssafy.dash.user.domain.User;

import java.time.LocalDateTime;

public record MessageResult(
        Long id,
        Long senderId,
        String senderName,
        String senderAvatar,
        String senderDecorationClass,
        String content,
        boolean isRead,
        LocalDateTime createdAt,
        boolean isMine // 프론트엔드용 헬퍼 필드
) {
    public static MessageResult from(DirectMessage dm, User sender, Long currentUserId) {
        return new MessageResult(
                dm.getId(),
                sender.getId(),
                sender.getUsername(),
                sender.getAvatarUrl(),
                sender.getEquippedDecorationClass(),
                dm.getContent(),
                dm.isRead(),
                dm.getCreatedAt(),
                sender.getId().equals(currentUserId)
        );
    }
}
