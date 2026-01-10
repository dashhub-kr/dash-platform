package com.ssafy.dash.social.application.dto.result;

import java.time.LocalDateTime;

public record ConversationResult(
        Long partnerId,
        String partnerName,
        String partnerAvatar,
        String partnerDecorationClass,
        String lastMessagePreview,
        LocalDateTime lastMessageTime,
        int unreadCount) {
    public static ConversationResult of(
            Long partnerId,
            String partnerName,
            String partnerAvatar,
            String partnerDecorationClass,
            String lastMessagePreview,
            LocalDateTime lastMessageTime,
            int unreadCount) {
        return new ConversationResult(
                partnerId,
                partnerName,
                partnerAvatar,
                partnerDecorationClass,
                lastMessagePreview != null && lastMessagePreview.length() > 50
                        ? lastMessagePreview.substring(0, 50) + "..."
                        : lastMessagePreview,
                lastMessageTime,
                unreadCount);
    }
}
