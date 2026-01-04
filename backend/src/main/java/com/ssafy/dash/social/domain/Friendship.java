package com.ssafy.dash.social.domain;

import java.time.LocalDateTime;

public class Friendship {
    private Long id;
    private Long requesterId;
    private Long receiverId;
    private FriendshipStatus status;
    private LocalDateTime createdAt;

    public enum FriendshipStatus {
        PENDING,
        ACCEPTED
    }

    public Friendship() {}

    public Friendship(Long requesterId, Long receiverId, FriendshipStatus status, LocalDateTime createdAt) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Friendship create(Long requesterId, Long receiverId) {
        return new Friendship(requesterId, receiverId, FriendshipStatus.PENDING, LocalDateTime.now());
    }

    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
    }

    public Long getId() {
        return id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
