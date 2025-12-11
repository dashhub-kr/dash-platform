package com.ssafy.dash.problem.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProblemTag {
    private Long id;
    private String problemNumber;
    private String tagKey;

    private ProblemTag(String problemNumber, String tagKey) {
        this.problemNumber = problemNumber;
        this.tagKey = tagKey;
    }

    public static ProblemTag create(String problemNumber, String tagKey) {
        return new ProblemTag(problemNumber, tagKey);
    }
}
