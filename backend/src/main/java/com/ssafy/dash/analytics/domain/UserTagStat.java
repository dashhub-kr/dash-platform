package com.ssafy.dash.analytics.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Solved.ac 태그별 사용자 통계
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTagStat {
    private Long id;
    private Long userId;
    private String tagKey;
    private Integer total;
    private Integer solved;
    private Integer partial;
    private Integer tried;
    private Integer rating;
    private Boolean isBasic;
    private LocalDateTime updatedAt;

    public static UserTagStat create(Long userId, String tagKey, Integer total,
            Integer solved, Integer partial, Integer tried, Integer rating) {
        UserTagStat stat = new UserTagStat();
        stat.userId = userId;
        stat.tagKey = tagKey;
        stat.total = total;
        stat.solved = solved;
        stat.partial = partial;
        stat.tried = tried;
        stat.rating = rating != null ? rating : 0;
        return stat;
    }

    /**
     * 마스터리 레벨 계산 (푼 문제 수 기반)
     */
    public String getMasteryLevel() {
        if (solved >= 50)
            return "MASTER";
        if (solved >= 30)
            return "EXPERT";
        if (solved >= 15)
            return "ADVANCED";
        if (solved >= 5)
            return "INTERMEDIATE";
        if (solved >= 1)
            return "BEGINNER";
        return "NONE";
    }

    /**
     * 마스터리 레벨에 해당하는 뱃지
     */
    public String getMasteryBadge() {
        return switch (getMasteryLevel()) {
            case "MASTER" -> "🏆";
            case "EXPERT" -> "⭐";
            case "ADVANCED" -> "⚡";
            case "INTERMEDIATE" -> "📚";
            case "BEGINNER" -> "🌱";
            default -> "⚪";
        };
    }

    /**
     * 다음 레벨까지 필요한 문제 수
     */
    public int getSolvedToNextLevel() {
        if (solved >= 50)
            return 0;
        if (solved >= 30)
            return 50 - solved;
        if (solved >= 15)
            return 30 - solved;
        if (solved >= 5)
            return 15 - solved;
        if (solved >= 1)
            return 5 - solved;
        return 1;
    }

    /**
     * 다음 레벨 이름
     */
    public String getNextLevel() {
        if (solved >= 50)
            return "MAX";
        if (solved >= 30)
            return "MASTER";
        if (solved >= 15)
            return "EXPERT";
        if (solved >= 5)
            return "ADVANCED";
        if (solved >= 1)
            return "INTERMEDIATE";
        return "BEGINNER";
    }

    /**
     * 완료율 (전체 문제 대비 푼 비율)
     */
    public Double getCompletionRate() {
        if (total == null || total == 0)
            return 0.0;
        return Math.min((double) solved / total * 100, 100.0);
    }
}
