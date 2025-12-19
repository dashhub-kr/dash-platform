package com.ssafy.dash.problem.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Problem {

    private String problemNumber;
    private String title;
    private Integer level;
    private Integer problemClass;
    private Boolean essential;
    private Boolean sprout;

    public static Problem create(String problemNumber, String title, Integer level, Integer problemClass,
            Boolean essential, Boolean sprout) {
        Problem problem = new Problem();
        problem.problemNumber = problemNumber;
        problem.title = title;
        problem.level = level;
        problem.problemClass = problemClass;
        problem.essential = essential;
        problem.sprout = sprout;
        return problem;
    }
}
