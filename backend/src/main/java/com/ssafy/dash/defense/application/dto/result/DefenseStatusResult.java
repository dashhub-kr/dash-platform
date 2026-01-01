package com.ssafy.dash.defense.application.dto.result;

import java.time.LocalDateTime;

public record DefenseStatusResult(
        String defenseType,
        Integer defenseProblemId,
        LocalDateTime defenseStartTime,
        Integer silverStreak,
        Integer goldStreak,
        Integer maxSilverStreak,
        Integer maxGoldStreak) {
}
