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
    // 분석 섹션
    private String analysisSummary;
    private String keyStrength;
    private String primaryWeakness;

    // 예측 섹션
    private Integer estimatedDaysToGoal;
    private String growthPrediction;

    // 전략 섹션
    private String weeklyGoal;
    private List<String> recommendedTags;
    private String difficultySuggestion;
    private String strategicAdvice;

    // 로드맵 섹션
    private List<LearningPhase> phases;
    private Integer nextClassToClear;

    // 동기부여
    private String motivationMessage;

    // 레거시 필드 (호환성)
    private String efficiencyAnalysis;
    private String personalizedAdvice;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LearningPhase {
        private int phase;
        private String title;
        private Integer durationDays;
        private List<String> focusTags;
        private Integer targetProblems;
        private String difficultyRange;
        private List<String> goals;
        private List<String> milestones;

        // 레거시 필드 (호환성)
        private String duration;
        private String focus;
    }
}
