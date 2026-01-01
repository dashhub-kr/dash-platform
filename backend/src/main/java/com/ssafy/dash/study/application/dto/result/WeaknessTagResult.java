package com.ssafy.dash.study.application.dto.result;

/**
 * 약점 태그 Result DTO
 */
public record WeaknessTagResult(
        String tagKey,
        double averageRate) {
}
