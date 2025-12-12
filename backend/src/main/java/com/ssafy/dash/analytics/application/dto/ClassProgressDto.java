package com.ssafy.dash.analytics.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 클래스별 진행 현황 DTO
 */
@Data
@Builder
public class ClassProgressDto {
    private Integer classNumber; // 클래스 번호 (1-10)
    private Double completionRate; // 전체 완료율 (%)
    private Double essentialCompletionRate; // 에센셜 완료율 (%)
    private String decoration; // 데코레이션 (gold, silver, bronze, none)
    private Integer totalProblems; // 전체 문제 수
    private Integer solvedProblems; // 푼 문제 수
    private Integer remainingEssentials; // 남은 에센셜 문제 수
}
