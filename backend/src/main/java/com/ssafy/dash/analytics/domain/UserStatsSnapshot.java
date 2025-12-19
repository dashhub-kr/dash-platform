package com.ssafy.dash.analytics.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 통계 스냅샷 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
public class UserStatsSnapshot {
    private Long id;
    private Long userId;
    private LocalDate snapshotDate;
    private Integer totalSolved;
    private Integer solvedacTier;
    private Integer solvedacRating;
    private Integer solvedacClass;
    private LocalDateTime createdAt;

    public static UserStatsSnapshot create(Long userId, LocalDate snapshotDate,
            Integer totalSolved, Integer tier, Integer rating, Integer classLevel) {
        UserStatsSnapshot snapshot = new UserStatsSnapshot();
        snapshot.userId = userId;
        snapshot.snapshotDate = snapshotDate;
        snapshot.totalSolved = totalSolved;
        snapshot.solvedacTier = tier;
        snapshot.solvedacRating = rating;
        snapshot.solvedacClass = classLevel;
        return snapshot;
    }
}
