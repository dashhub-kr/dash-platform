package com.ssafy.dash.ai.application.dto;

import java.util.List;

public record HintChatCommand(
        Long userId,
        Long recordId,
        String message,
        String solveStatus,
        String wrongReason,
        List<ChatMessage> history,
        String problemNumber,
        String problemTitle,
        String code,
        String language,
        UserContext userContext) {

    public record ChatMessage(String role, String content) {}

    public record UserContext(String tierName, int solvedCount, List<String> weakTags) {}
}
