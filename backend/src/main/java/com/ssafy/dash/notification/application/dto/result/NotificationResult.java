package com.ssafy.dash.notification.application.dto.result;

import java.time.LocalDateTime;

import com.ssafy.dash.notification.domain.Notification;
import com.ssafy.dash.notification.domain.NotificationType;

public record NotificationResult(
        Long id,
        Long receiverId,
        String content,
        String url,
        NotificationType type,
        boolean isRead,
        LocalDateTime createdAt) {
    public static NotificationResult from(Notification notification) {
        return new NotificationResult(
                notification.getId(),
                notification.getReceiverId(),
                notification.getContent(),
                notification.getUrl(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}
