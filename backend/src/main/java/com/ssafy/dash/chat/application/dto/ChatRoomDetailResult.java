package com.ssafy.dash.chat.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatRoomDetailResult(
        Long id,
        String name,
        String type,
        Long studyId,
        Long creatorId,
        LocalDateTime createdAt,
        List<MemberInfo> members) {
    public record MemberInfo(
            Long userId,
            String username,
            String avatarUrl) {
    }
}
