package com.ssafy.dash.study.application.dto.result;

import java.time.LocalDate;
import java.util.List;

import com.ssafy.dash.study.domain.StudyMission.MissionStatus;

/**
 * 미션 진행 현황 Result DTO
 */
public record MissionWithProgressResult(
        Long id,
        Integer week,
        String title,
        List<Integer> problemIds,
        String sourceType,
        LocalDate deadline,
        MissionStatus status,
        int solvedCount,
        int totalProblems,
        List<MemberProgressResult> memberProgressList) {
}
