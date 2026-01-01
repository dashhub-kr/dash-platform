package com.ssafy.dash.study.application.dto.result;

import java.util.Map;

/**
 * 멤버별 태그 통계 Result DTO
 */
public record MemberTagStatsResult(
        Long userId,
        String username,
        Integer tier,
        String avatarUrl,
        Map<String, Double> tagRates,
        Map<String, Integer> tagSolved) {
}
