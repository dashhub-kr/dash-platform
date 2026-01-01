package com.ssafy.dash.analytics.application.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 학습 단계별 추천 세트 DTO
 */
@Data
@Builder
public class RecommendationSetDto {
    private Integer phase; // 단계 번호 (1, 2, 3...)
    private String title; // 단계 제목 (약점 보완: DP)
    private String category; // 카테고리 (약점보완, 클래스완성, 강점강화)
    private String reason; // 추천 이유
    private List<RecommendedProblemDto> problems; // 추천 문제 목록
}
