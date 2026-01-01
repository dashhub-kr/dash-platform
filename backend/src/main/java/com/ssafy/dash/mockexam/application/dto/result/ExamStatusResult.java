package com.ssafy.dash.mockexam.application.dto.result;

import java.time.LocalDateTime;
import java.util.List;

public record ExamStatusResult(
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
