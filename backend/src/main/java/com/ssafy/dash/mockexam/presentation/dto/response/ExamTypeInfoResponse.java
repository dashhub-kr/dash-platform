package com.ssafy.dash.mockexam.presentation.dto.response;

/**
 * 모의고사 타입 정보 응답 DTO
 */
public record ExamTypeInfoResponse(
        String type,
        String displayName,
        String category,
        int problemCount,
        int timeLimitHours,
        String description) {
}
