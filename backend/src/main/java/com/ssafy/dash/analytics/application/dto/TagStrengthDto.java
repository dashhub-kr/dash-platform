package com.ssafy.dash.analytics.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 강점 태그 분석 결과 DTO
 */
@Data
@Builder
public class TagStrengthDto {
    private String tagKey;
    private String tagName;
    private Integer solved;
    private String masteryLevel; // MASTER, EXPERT, ADVANCED, INTERMEDIATE, BEGINNER
    private Double relativeStrength; // 사용자 내 상대 점수 (0-100)
    private String badge; // 🏆, ⭐, ⚡, 📚, 🌱
}
