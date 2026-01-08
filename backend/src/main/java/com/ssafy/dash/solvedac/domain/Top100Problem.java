package com.ssafy.dash.solvedac.domain;

import java.util.List;

/**
 * Top 100 문제 도메인 모델
 */
public record Top100Problem(
        int problemId,
        String title,
        int level,
        List<String> tags) {
}
