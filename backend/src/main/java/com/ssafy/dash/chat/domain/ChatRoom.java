package com.ssafy.dash.chat.domain;

import java.time.LocalDateTime;

public class ChatRoom {
    private Long id;
    private String name;
    private ChatRoomType type;
    private Long studyId;
    private Long creatorId;
    private LocalDateTime createdAt;

    // Transient fields for display
    private int memberCount;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;

    public enum ChatRoomType {
        STUDY, GROUP
    }

    public ChatRoom() {
    }

    public ChatRoom(String name, ChatRoomType type, Long studyId, Long creatorId) {
        this.name = name;
        this.type = type;
        this.studyId = studyId;
        this.creatorId = creatorId;
        this.createdAt = LocalDateTime.now();
    }

    public static ChatRoom createStudyRoom(String name, Long studyId, Long creatorId) {
        return new ChatRoom(name, ChatRoomType.STUDY, studyId, creatorId);
    }

    public static ChatRoom createGroupRoom(String name, Long creatorId) {
        return new ChatRoom(name, ChatRoomType.GROUP, null, creatorId);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChatRoomType getType() {
        return type;
    }

    public void setType(ChatRoomType type) {
        this.type = type;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
