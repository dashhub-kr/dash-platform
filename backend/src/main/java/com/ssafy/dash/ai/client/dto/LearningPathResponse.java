package com.ssafy.dash.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 학습 경로 추천 응답 DTO
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LearningPathResponse {
    private String overallAssessment; // 현재 상태 종합 평가
    private String keyStrength; // 핵심 강점
    private String primaryWeakness; // 주요 약점
    private String personalizedAdvice; // 개인화 조언
    private List<LearningPhase> phases; // AI 추천 학습 단계
    private String motivationalMessage; // 동기부여 메시지

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LearningPhase {
        private int priority; // 우선순위 (1, 2, 3)
        private String title; // 단계 제목
        private String description; // 상세 설명
        private String estimatedTime; // 예상 소요 시간
        private List<String> actionItems; // 구체적 실천 항목
    }
}
