package com.ssafy.dash.common.fixtures;

import java.time.LocalDateTime;

public final class FixtureTime {

    private static final LocalDateTime FIXED_TIMESTAMP = LocalDateTime.of(2025, 12, 4, 0, 0);

    private FixtureTime() {
    }

    public static LocalDateTime now() {
        return FIXED_TIMESTAMP;
    }

}
