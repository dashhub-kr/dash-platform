package com.ssafy.dash.analytics.application.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 성장 추세 분석 결과 DTO
 */
@Data
@Builder
public class GrowthTrendDto {
    private String period; // "30일"
    private Integer totalGrowth; // 기간 내 성장한 문제 수
    private Double dailyAverage; // 일평균 풀이 수
    private String trend; // GROWING, STABLE, DECLINING
    private String trendEmoji; // 📈, ➡️, 📉
    private List<TagGrowthDto> topGrowthTags; // 가장 성장한 태그 TOP 5
    private String recommendation; // 추천 메시지
}
