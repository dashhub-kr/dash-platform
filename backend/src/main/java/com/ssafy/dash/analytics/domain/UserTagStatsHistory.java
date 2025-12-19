package com.ssafy.dash.analytics.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 사용자 태그별 통계 히스토리 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
public class UserTagStatsHistory {
    private Long id;
    private Long snapshotId;
    private Long userId;
    private String tagKey;
    private Integer solved;
    private LocalDateTime createdAt;

    public static UserTagStatsHistory create(Long snapshotId, Long userId,
            String tagKey, Integer solved) {
        UserTagStatsHistory history = new UserTagStatsHistory();
        history.snapshotId = snapshotId;
        history.userId = userId;
        history.tagKey = tagKey;
        history.solved = solved;
        return history;
    }
}
