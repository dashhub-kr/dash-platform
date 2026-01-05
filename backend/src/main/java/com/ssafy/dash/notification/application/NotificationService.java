package com.ssafy.dash.notification.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.notification.application.dto.result.NotificationResult;
import com.ssafy.dash.notification.domain.Notification;
import com.ssafy.dash.notification.domain.NotificationRepository;
import com.ssafy.dash.notification.domain.NotificationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void send(Long receiverId, String content, String url, NotificationType type) {
        send(receiverId, content, url, type, null);
    }

    public void send(Long receiverId, String content, String url, NotificationType type, Long relatedId) {
        Notification notification = Notification.create(receiverId, content, url, type, relatedId);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResult> getNotifications(Long receiverId) {
        return notificationRepository.findTop50ByReceiverIdOrderByCreatedAtDesc(receiverId).stream()
                .map(NotificationResult::from)
                .collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.markAsRead();
            notificationRepository.update(notification);
        });
    }

    public void markAllAsRead(Long receiverId) {
        notificationRepository.markAllAsRead(receiverId);
    }

    public void update(Long id, String content, NotificationType type) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.updateContent(content, type);
            notificationRepository.update(notification);
        });
    }
}
