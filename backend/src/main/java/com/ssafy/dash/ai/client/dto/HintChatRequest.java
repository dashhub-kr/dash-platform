package com.ssafy.dash.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI 튜터 대화 요청 DTO
 */
@Getter
@Builder
public class HintChatRequest {
    private String message;

    @JsonProperty("problemNumber")
    private String problemNumber;

    @JsonProperty("problemTitle")
    private String problemTitle;

    private String code;
    private String language;

    @JsonProperty("solveStatus")
    private String solveStatus; // "solved" | "wrong"

    @JsonProperty("wrongReason")
    private String wrongReason; // 틀린 이유 (시간초과, 틀렸습니다 등)

    private List<ChatMessage> history;

    @JsonProperty("userContext")
    private UserContext userContext;

    @Getter
    @Builder
    public static class ChatMessage {
        private String role;
        private String content;
    }

    @Getter
    @Builder
    public static class UserContext {
        @JsonProperty("tierName")
        private String tierName;

        @JsonProperty("solvedCount")
        private int solvedCount;

        @JsonProperty("weakTags")
        private List<String> weakTags;
    }
}
