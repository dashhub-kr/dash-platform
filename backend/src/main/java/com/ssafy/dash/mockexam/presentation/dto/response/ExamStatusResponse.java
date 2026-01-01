package com.ssafy.dash.mockexam.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 모의고사 상태 응답 DTO
 */
public record ExamStatusResponse(
        String examType,
        String displayName,
        String category,
        List<Integer> problems,
        List<Integer> solvedProblems,
        LocalDateTime startTime,
        int timeLimitHours,
        int solvedCount,
        int totalCount) {
}
