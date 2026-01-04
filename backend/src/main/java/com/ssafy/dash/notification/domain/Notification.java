package com.ssafy.dash.notification.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private Long id;
    private Long receiverId;
    private String content;
    private String url;
    private Long relatedId;
    private NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static Notification create(Long receiverId, String content, String url, NotificationType type) {
        return create(receiverId, content, url, type, null);
    }

    public static Notification create(Long receiverId, String content, String url, NotificationType type,
            Long relatedId) {
        return Notification.builder()
                .receiverId(receiverId)
                .content(content)
                .url(url)
                .type(type)
                .relatedId(relatedId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void updateContent(String content, NotificationType type) {
        if (content != null) {
            this.content = content;
        }
        if (type != null) {
            this.type = type;
        }
    }
}
