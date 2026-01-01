package com.ssafy.dash.defense.presentation.dto.response;

import java.time.LocalDateTime;

public record DefenseStatusResponse(
    String defenseType,
    Integer defenseProblemId,
    LocalDateTime defenseStartTime,
    Integer silverStreak,
    Integer goldStreak,
    Integer maxSilverStreak,
    Integer maxGoldStreak
) {}
