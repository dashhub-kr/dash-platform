package com.ssafy.dash.problem.presentation.dto.response;

import com.ssafy.dash.problem.domain.Problem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemRecommendationResponse {
    private String problemId;
    private String title;
    private Integer level;
    private List<String> tags;

    public static ProblemRecommendationResponse from(Problem problem, List<String> tags) {
        return new ProblemRecommendationResponse(
                problem.getProblemNumber(),
                problem.getTitle(),
                problem.getLevel(),
                tags);
    }
}
