package com.ssafy.dash.notification.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.notification.domain.Notification;
import com.ssafy.dash.notification.domain.NotificationRepository;
import com.ssafy.dash.notification.infrastructure.mapper.NotificationMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

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
    public List<Notification> findTop50ByReceiverIdOrderByCreatedAtDesc(Long receiverId) {
        return mapper.findTop50ByReceiverIdOrderByCreatedAtDesc(receiverId);
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
