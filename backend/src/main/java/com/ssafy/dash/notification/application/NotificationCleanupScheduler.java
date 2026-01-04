package com.ssafy.dash.notification.application;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.notification.domain.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupScheduler {

    private final NotificationRepository notificationRepository;

    // 매일 새벽 3시에 실행
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldNotifications() {
        log.info("Starting notification cleanup...");

        // 14일 이전
        LocalDateTime threshold = LocalDateTime.now().minusDays(14);

        notificationRepository.deleteReadBefore(threshold);

        log.info("Notification cleanup completed. Deleted read notifications before {}", threshold);
    }
}
