package com.ssafy.dash.solvedac.domain;

import java.util.List;

public record SolvedProblem(
        int problemId,
        String title,
        int level,
        List<String> tags) {
}
