package com.ssafy.dash.notification.infrastructure.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.notification.domain.Notification;

@Mapper
public interface NotificationMapper {
    void save(Notification notification);

    void update(Notification notification);

    Optional<Notification> findById(Long id);

    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    void markAllAsRead(Long receiverId);

    void deleteReadBefore(java.time.LocalDateTime before);
}
