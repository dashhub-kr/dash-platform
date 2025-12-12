package com.ssafy.dash.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 튜터 대화 응답 DTO
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TutorChatResponse {
    private String reply; // AI 응답
    private String teachingStyle; // 교수법 ("socratic", "direct", "hint")
    private List<String> followUpQuestions; // 후속 질문 제안
    private String conceptExplanation; // 개념 설명 (있을 경우)
    private String encouragement; // 격려 메시지
}
