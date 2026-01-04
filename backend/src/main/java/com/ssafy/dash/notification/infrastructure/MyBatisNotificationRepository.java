package com.ssafy.dash.notification.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.notification.domain.Notification;
import com.ssafy.dash.notification.domain.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MyBatisNotificationRepository implements NotificationRepository {

    private final NotificationMapper mapper;

    @Override
    public void save(Notification notification) {
        mapper.save(notification);
    }

    @Override
    public void update(Notification notification) {
        mapper.update(notification);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId) {
        return mapper.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    @Override
    public void markAllAsRead(Long receiverId) {
        mapper.markAllAsRead(receiverId);
    }

    @Override
    public void deleteReadBefore(java.time.LocalDateTime before) {
        mapper.deleteReadBefore(before);
    }
}
