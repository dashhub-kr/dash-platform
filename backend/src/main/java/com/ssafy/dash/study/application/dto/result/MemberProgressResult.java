package com.ssafy.dash.study.application.dto.result;

import java.util.List;

/**
 * 멤버별 미션 진행 현황 Result DTO
 */
public record MemberProgressResult(
        Long userId,
        String username,
        String avatarUrl,
        int completedCount,
        int totalProblems,
        boolean allCompleted,
        List<Integer> solvedProblemIds,
        List<Integer> sosProblemIds) {
}
