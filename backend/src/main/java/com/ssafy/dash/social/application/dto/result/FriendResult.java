package com.ssafy.dash.social.application.dto.result;

import com.ssafy.dash.social.domain.Friendship;
import com.ssafy.dash.user.application.dto.result.UserResult;
import com.ssafy.dash.user.domain.User;

import java.time.LocalDateTime;

public record FriendResult(
        Long id, // Friendship ID
        UserResult friend,
        Friendship.FriendshipStatus status,
        LocalDateTime createdAt
) {
    public static FriendResult from(Friendship friendship, User friendUser) {
        return new FriendResult(
                friendship.getId(),
                UserResult.from(friendUser),
                friendship.getStatus(),
                friendship.getCreatedAt()
        );
    }
}
