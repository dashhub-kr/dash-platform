package com.ssafy.dash.user.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {

    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private String provider;
    private String providerId;
    private String avatarUrl;
    private LocalDateTime deletedAt;

    private User(Long id, String username, String email, LocalDateTime createdAt,
                 String provider, String providerId, String avatarUrl) {
        this.id = id;
        this.username = requireText(username, "username");
        this.email = requireText(email, "email");
        this.createdAt = requireTimestamp(createdAt);
        this.provider = provider;
        this.providerId = providerId;
        this.avatarUrl = avatarUrl;
    }

    public void markDeleted(LocalDateTime deletedAt) {
        this.deletedAt = Objects.requireNonNullElse(deletedAt, LocalDateTime.now());
    }

    public static User create(String username, String email, LocalDateTime createdAt,
                              String provider, String providerId, String avatarUrl) {
        return new User(null, username, email, createdAt, provider, providerId, avatarUrl);
    }

    public void updateProfile(String username, String email, String avatarUrl) {
        this.username = requireText(username, "username");
        this.email = requireText(email, "email");
        this.avatarUrl = avatarUrl;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static LocalDateTime requireTimestamp(LocalDateTime value) {
        return Objects.requireNonNull(value, "createdAt" + " must not be null");
    }

}
