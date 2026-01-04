package com.ssafy.dash.notification.presentation.dto.response;

import java.time.LocalDateTime;

import com.ssafy.dash.notification.application.dto.result.NotificationResult;
import com.ssafy.dash.notification.domain.NotificationType;

public record NotificationResponse(
        Long id,
        String content,
        String url,
        NotificationType type,
        boolean isRead,
        LocalDateTime createdAt) {
    public static NotificationResponse from(NotificationResult result) {
        return new NotificationResponse(
                result.id(),
                result.content(),
                result.url(),
                result.type(),
                result.isRead(),
                result.createdAt());
    }
}
