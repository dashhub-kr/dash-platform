package com.ssafy.dash.chat.application.dto;

import java.time.LocalDateTime;

public record ChatRoomMessageResult(
        Long id,
        Long senderId,
        String senderUsername,
        String senderAvatarUrl,
        String content,
        LocalDateTime createdAt,
        boolean isMe) {
}
