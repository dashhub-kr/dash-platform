package com.ssafy.dash.notification.domain;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    void save(Notification notification);

    void update(Notification notification);

    Optional<Notification> findById(Long id);

    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    void markAllAsRead(Long receiverId);

    void deleteReadBefore(java.time.LocalDateTime before);
}
