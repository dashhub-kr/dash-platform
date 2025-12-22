package com.ssafy.dash.ai.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Learning Path 일별 캐시 엔티티
 * AI 분석 결과를 하루 단위로 캐싱합니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathCache {

    private Long id;
    private Long userId;
    private String analysisJson; // Full LearningDashboardResponse as JSON
    private LocalDate generatedAt; // 분석 생성 날짜
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
