package com.ssafy.dash.analytics.application.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 학습 밸런스 분석 결과 DTO
 */
@Data
@Builder
public class BalanceAnalysisDto {
    private Double balanceScore; // 0-100, 높을수록 균형있음
    private String balanceType; // BALANCED, FOCUSED, SPECIALIZED
    private Double topThreeConcentration; // 상위 3개 태그 집중도 (%)
    private List<String> focusedTags; // 집중된 상위 태그 이름들
    private String recommendation; // 추천 메시지
}
