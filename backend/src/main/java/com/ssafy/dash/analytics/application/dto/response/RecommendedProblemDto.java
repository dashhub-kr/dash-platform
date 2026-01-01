package com.ssafy.dash.analytics.application.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 추천 문제 DTO
 */
@Data
@Builder
public class RecommendedProblemDto {
    private Integer problemNumber; // 문제 번호
    private String title; // 문제 제목
    private Integer tier; // 난이도 티어
    private String tierName; // 티어 이름 (Bronze V 등)
    private List<String> tags; // 태그 목록
}
