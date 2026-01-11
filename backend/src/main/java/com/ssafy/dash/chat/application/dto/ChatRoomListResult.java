package com.ssafy.dash.chat.application.dto;

import java.time.LocalDateTime;

public record ChatRoomListResult(
        Long id,
        String name,
        String type,
        Long studyId,
        int memberCount,
        String lastMessage,
        LocalDateTime lastMessageAt,
        int unreadCount) {
}
