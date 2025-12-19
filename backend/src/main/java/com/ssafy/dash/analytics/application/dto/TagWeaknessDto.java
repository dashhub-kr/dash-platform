package com.ssafy.dash.analytics.application.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 약점 태그 분석 결과 DTO
 */
@Data
@Builder
public class TagWeaknessDto {
    private String tagKey;
    private String tagName;
    private Integer solved;
    private String nextLevel; // 다음 레벨
    private Integer solvedToNextLevel; // 다음 레벨까지 필요한 문제 수
    private String recommendation; // 추천 메시지
    private List<Integer> suggestedProblems; // 추천 문제 번호
}
