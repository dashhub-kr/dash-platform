package com.ssafy.dash.study.presentation.dto.response;

import com.ssafy.dash.study.application.dto.result.StudyStatsResult;

public record StudyStatsResponse(
        long bronze,
        long silver,
        long gold,
        long platinum
) {
    public static StudyStatsResponse from(StudyStatsResult result) {
        return new StudyStatsResponse(
                result.bronze(),
                result.silver(),
                result.gold(),
                result.platinum()
        );
    }
}
