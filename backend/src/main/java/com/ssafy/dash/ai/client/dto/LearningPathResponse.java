package com.ssafy.dash.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 학습 경로 추천 응답 DTO
 * AI 서버 LearningPathResponse 스키마와 매핑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LearningPathResponse {
    private String analysisSummary; // 분석 요약 (객관적 사실)
    private String growthPrediction; // 성장 예측
    private String strategicAdvice; // 전략적 조언
    private String efficiencyAnalysis; // 효율성 분석
    private String keyStrength; // 핵심 강점
    private String primaryWeakness; // 주요 약점
    private String personalizedAdvice; // 개인화 조언
    private List<LearningPhase> phases; // AI 추천 학습 단계

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LearningPhase {
        private int phase; // Phase 번호
        private String title; // 단계 제목
        private String duration; // 기간 (예: "2주")
        private String focus; // 집중 분야
        private List<String> goals; // 목표
        private List<String> milestones; // 마일스톤
    }
}
