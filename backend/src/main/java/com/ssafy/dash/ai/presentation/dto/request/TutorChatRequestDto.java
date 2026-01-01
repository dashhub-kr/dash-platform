package com.ssafy.dash.ai.presentation.dto.request;

public record TutorChatRequestDto(
        Long userId,
        String sessionId,
        String message,
        String problemNumber,
        String code) {
}
