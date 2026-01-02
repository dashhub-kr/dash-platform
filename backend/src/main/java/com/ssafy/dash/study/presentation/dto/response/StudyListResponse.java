package com.ssafy.dash.study.presentation.dto.response;

import com.ssafy.dash.study.domain.Study;

/**
 * 스터디 목록 응답 DTO
 */
public record StudyListResponse(
        Long id,
        String name,
        Integer memberCount,
        Double averageTier,
        String tierBadge, // 티어 뱃지 (Bronze, Silver, Gold 등)
        Integer totalSolved,
        Integer acornCount,
        String mvpUsername,
        Integer streak,
        Double averageSubmissionRate) {

    public static StudyListResponse from(Study study) {
        return new StudyListResponse(
                study.getId(),
                study.getName(),
                study.getMemberCount() != null ? study.getMemberCount() : 0,
                study.getAverageTier(),
                getTierBadge(study.getAverageTier()),
                study.getTotalSolved() != null ? study.getTotalSolved() : 0,
                study.getAcornCount() != null ? study.getAcornCount() : 0,
                study.getMvpUsername(),
                study.getStreak() != null ? study.getStreak() : 0,
                study.getAverageSubmissionRate() != null ? study.getAverageSubmissionRate() : 0.0);
    }

    private static String getTierBadge(Double tier) {
        if (tier == null)
            return "Unranked";
        int t = tier.intValue();
        if (t >= 26)
            return "Ruby";
        if (t >= 21)
            return "Diamond";
        if (t >= 16)
            return "Platinum";
        if (t >= 11)
            return "Gold";
        if (t >= 6)
            return "Silver";
        if (t >= 1)
            return "Bronze";
        return "Unranked";
    }
}
