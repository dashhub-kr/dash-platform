package com.ssafy.dash.ai.presentation.dto.request;

import java.util.List;

public record HintChatRequestDto(
        Long userId,
        Long recordId,
        String message,
        String solveStatus,
        String wrongReason,
        List<ChatMessageDto> history,
        String problemNumber,
        String problemTitle,
        String code,
        String language,
        UserContextDto userContext) {
}
