package com.ssafy.dash.analytics.application.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 난이도(클래스) 분포 분석 결과 DTO
 */
@Data
@Builder
public class DifficultyAnalysisDto {
    private Integer strongestClass; // 가장 잘하는 클래스
    private Integer targetClass; // 도전해야 할 클래스
    private List<ClassProgressDto> classProgress; // 클래스별 진행 현황
    private String recommendation; // 추천 메시지
}
