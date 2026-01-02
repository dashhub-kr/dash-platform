package com.ssafy.dash.study.presentation.dto.request;

import java.time.LocalDate;

/**
 * 미션 정보 수정 요청 DTO
 */
public record UpdateMissionRequest(
        String title,
        LocalDate deadline,
        java.util.List<Integer> problemIds) {
}
