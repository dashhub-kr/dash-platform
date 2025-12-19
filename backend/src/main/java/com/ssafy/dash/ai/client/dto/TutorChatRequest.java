package com.ssafy.dash.ai.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 튜터 대화 요청 DTO
 */
@Getter
@Builder
public class TutorChatRequest {
    private String message; // 사용자 메시지
    private String problemNumber; // 관련 문제 번호 (선택)
    private String code; // 관련 코드 (선택)
    private List<ChatMessage> history; // 대화 히스토리
    private UserContext context; // 사용자 컨텍스트

    @Getter
    @Builder
    public static class ChatMessage {
        private String role; // "user" 또는 "assistant"
        private String content;
    }

    @Getter
    @Builder
    public static class UserContext {
        private String tier;
        private int solvedCount;
        private List<String> recentTags;
    }
}
