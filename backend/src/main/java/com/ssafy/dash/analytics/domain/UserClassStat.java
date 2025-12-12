package com.ssafy.dash.analytics.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Solved.ac 클래스별 사용자 통계
 */
@Getter
@Setter
@NoArgsConstructor
public class UserClassStat {
    private Long id;
    private Long userId;
    private Integer classNumber;
    private Integer total;
    private Integer totalSolved;
    private Integer essentials;
    private Integer essentialSolved;
    private String decoration;
    private LocalDateTime updatedAt;

    public static UserClassStat create(Long userId, Integer classNumber, Integer total,
            Integer totalSolved, Integer essentials,
            Integer essentialSolved, String decoration) {
        UserClassStat stat = new UserClassStat();
        stat.userId = userId;
        stat.classNumber = classNumber;
        stat.total = total;
        stat.totalSolved = totalSolved;
        stat.essentials = essentials;
        stat.essentialSolved = essentialSolved;
        stat.decoration = decoration;
        return stat;
    }

    /**
     * 클래스 전체 문제 완료율 계산
     */
    public double getCompletionRate() {
        return total > 0 ? (totalSolved * 100.0 / total) : 0.0;
    }

    /**
     * 에센셜 문제 완료율 계산
     */
    public double getEssentialCompletionRate() {
        return essentials > 0 ? (essentialSolved * 100.0 / essentials) : 0.0;
    }
}
