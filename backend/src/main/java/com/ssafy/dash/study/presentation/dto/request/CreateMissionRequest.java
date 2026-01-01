package com.ssafy.dash.study.presentation.dto.request;

import java.time.LocalDate;
import java.util.List;

/**
 * 미션 생성 요청 DTO
 */
public record CreateMissionRequest(
        Integer week,
        String title,
        List<Integer> problemIds,
        LocalDate deadline) {
}
