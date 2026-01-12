package com.ssafy.dash.ai.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 튜터 대화 엔티티
 * 
 * 멀티턴 대화 지원을 위해 세션 기반으로 대화를 저장합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TutorConversation {

    private Long id;
    private Long userId;
    private String sessionId;
    private String role; // "user" or "assistant"
    private String content;
    private String problemNumber;
    private LocalDateTime createdAt;

    @Builder
    public TutorConversation(Long userId, String sessionId, String role, String content, String problemNumber) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.problemNumber = problemNumber;
    }

    public static TutorConversation create(Long userId, String sessionId, String role, String content,
            String problemNumber) {
        return TutorConversation.builder()
                .userId(userId)
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .problemNumber(problemNumber)
                .build();
    }
}
