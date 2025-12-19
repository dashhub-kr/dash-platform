package com.ssafy.dash.analytics.application.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 학습 경로 추천 DTO
 */
@Data
@Builder
public class LearningPathDto {
    private String currentLevel; // 현재 레벨 (Silver 1, Class 3)
    private String nextGoal; // 다음 목표
    private List<RecommendationSetDto> phases; // 학습 단계별 추천
}
