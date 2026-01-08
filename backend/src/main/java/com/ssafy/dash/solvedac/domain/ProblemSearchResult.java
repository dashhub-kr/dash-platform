package com.ssafy.dash.solvedac.domain;

import java.util.List;

public record ProblemSearchResult(
        int totalCount,
        List<SolvedProblem> problems) {
}
