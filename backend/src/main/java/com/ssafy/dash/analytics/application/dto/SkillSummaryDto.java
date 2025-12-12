package com.ssafy.dash.analytics.application.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 사용자 스킬 종합 요약 DTO
 */
@Data
@Builder
public class SkillSummaryDto {
    private String tier; // Silver 1
    private Integer tierNumber; // 11
    private Integer classLevel; // 3
    private Integer totalSolved; // 153
    private Integer rating; // 892

    private List<TagStrengthDto> topStrengths; // 강점 TOP 5
    private List<TagWeaknessDto> weaknesses; // 약점 태그
    private List<String> recommendedTags; // 추천 학습 태그

    private String overallLevel; // "균형잡힌 실력자"
    private String nextGoal; // "다음 목표: Class 2 완성"
}
