package com.ssafy.dash.analytics.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Solved.ac 패밀리 태그별 사용자 통계 (집계)
 */
@Getter
@Setter
@NoArgsConstructor
public class UserFamilyStat {
    private String familyKey; // e.g., GRAPH, DP
    private String familyName; // e.g., 그래프, 다이나믹 프로그래밍
    private Integer total; // 전체 문제 수 (해당 패밀리에 속한 태그들의 총합)
    private Integer solved; // 푼 문제 수
    private Integer partial;
    private Integer tried;
    private Double rawScore; // 가중치 점수 (solved * weight)

    public static UserFamilyStat create(String familyKey, String familyName, Integer total, Integer solved,
            Integer partial, Integer tried, Double rawScore) {
        UserFamilyStat stat = new UserFamilyStat();
        stat.familyKey = familyKey;
        stat.familyName = familyName;
        stat.total = total;
        stat.solved = solved;
        stat.partial = partial;
        stat.tried = tried;
        stat.rawScore = rawScore;
        return stat;
    }

    /**
     * 마스터리 레벨 계산 (푼 문제 수 기반 - 패밀리는 태그 합산이므로 기준이 더 높아야 할 수 있음, 일단 동일하게 적용)
     */
    public String getMasteryLevel() {
        if (solved >= 100)
            return "MASTER";
        if (solved >= 60)
            return "EXPERT";
        if (solved >= 30)
            return "ADVANCED";
        if (solved >= 10)
            return "INTERMEDIATE";
        if (solved >= 3)
            return "BEGINNER";
        return "NONE";
    }

    /**
     * 완료율
     */
    public Double getCompletionRate() {
        if (total == null || total == 0)
            return 0.0;
        return Math.min((double) solved / total * 100, 100.0);
    }
}
