package com.ssafy.dash.analytics.application.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 태그별 성장 DTO
 */
@Data
@Builder
public class TagGrowthDto {
    private String tagKey;
    private String tagName;
    private Integer growth; // 성장한 문제 수
    private Integer previousSolved; // 이전 푼 문제 수
    private Integer currentSolved; // 현재 푼 문제 수
    private String growthEmoji; // 🔥, ⬆️, ➡️
}
