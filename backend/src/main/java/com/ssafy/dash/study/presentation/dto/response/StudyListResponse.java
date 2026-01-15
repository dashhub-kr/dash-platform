package com.ssafy.dash.study.presentation.dto.response;

import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.user.domain.User;

import java.time.LocalDate;
import java.util.List;

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
        LocalDate streakUpdatedAt, // streak 유효성 판단용
        Double averageSubmissionRate,
        List<MemberPreview> memberPreviews, // 멤버 미리보기 (프론트에서 표시 개수 조절)
        String description) {

    public static StudyListResponse from(Study study, List<User> members) {
        List<MemberPreview> previews = members != null
                ? members.stream()
                        .map(MemberPreview::from)
                        .toList()
                : List.of();

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
                study.getStreakUpdatedAt(),
                study.getAverageSubmissionRate() != null ? study.getAverageSubmissionRate() : 0.0,
                previews,
                study.getDescription());
    }

    private static String getTierBadge(Double tier) {
        if (tier == null || tier == 0)
            return "Unranked";

        int t = (int) Math.round(tier);
        if (t < 1)
            return "Unranked";
        if (t >= 31)
            return "Master";

        String[] ranks = { "Bronze", "Silver", "Gold", "Platinum", "Diamond", "Ruby" };
        int rankIndex = (t - 1) / 5;
        int subRank = 5 - ((t - 1) % 5);

        if (rankIndex >= 0 && rankIndex < ranks.length) {
            return ranks[rankIndex] + " " + toRoman(subRank);
        }

        return "Unranked";
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> "";
        };
    }
}
