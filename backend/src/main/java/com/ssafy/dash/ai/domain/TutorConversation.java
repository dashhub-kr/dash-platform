package com.ssafy.dash.ai.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 튜터 대화 엔티티
 * 
 * 멀티턴 대화 지원을 위해 세션 기반으로 대화를 저장합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorConversation {

    private Long id;
    private Long userId;
    private String sessionId;
    private String role; // "user" or "assistant"
    private String content;
    private String problemNumber;
    private LocalDateTime createdAt;

    /**
     * 사용자 메시지 생성
     */
    public static TutorConversation userMessage(Long userId, String sessionId, String content, String problemNumber) {
        return TutorConversation.builder()
                .userId(userId)
                .sessionId(sessionId)
                .role("user")
                .content(content)
                .problemNumber(problemNumber)
                .build();
    }

    /**
     * AI 응답 메시지 생성
     */
    public static TutorConversation assistantMessage(Long userId, String sessionId, String content) {
        return TutorConversation.builder()
                .userId(userId)
                .sessionId(sessionId)
                .role("assistant")
                .content(content)
                .build();
    }
}
