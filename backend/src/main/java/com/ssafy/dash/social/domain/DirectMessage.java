package com.ssafy.dash.social.domain;

import java.time.LocalDateTime;

public class DirectMessage {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;

    public DirectMessage() {}

    public DirectMessage(Long senderId, Long receiverId, String content, LocalDateTime createdAt) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.isRead = false;
        this.createdAt = createdAt;
    }

    public static DirectMessage create(Long senderId, Long receiverId, String content) {
        return new DirectMessage(senderId, receiverId, content, LocalDateTime.now());
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public Long getId() {
        return id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
