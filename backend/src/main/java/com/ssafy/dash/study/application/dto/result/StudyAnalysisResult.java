package com.ssafy.dash.study.application.dto.result;

import java.util.List;
import java.util.Map;

/**
 * 스터디 분석 Result DTO
 */
public record StudyAnalysisResult(
        List<MemberTagStatsResult> memberStats,
        Map<String, Double> teamAverages,
        double averageTier,
        List<WeaknessTagResult> topWeaknesses) {
    
    public static StudyAnalysisResult empty() {
        return new StudyAnalysisResult(List.of(), Map.of(), 0.0, List.of());
    }
}
