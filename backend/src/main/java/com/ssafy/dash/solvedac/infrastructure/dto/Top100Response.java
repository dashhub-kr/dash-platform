package com.ssafy.dash.solvedac.infrastructure.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Solved.ac Top 100 API 응답 DTO
 * GET /user/top_100?handle={handle}
 */
@Data
@NoArgsConstructor
public class Top100Response {

    private int count;
    private List<Problem> items;

    @Data
    @NoArgsConstructor
    public static class Problem {
        private int problemId;
        private String titleKo;
        private int level;
        private List<Tag> tags;
    }

    @Data
    @NoArgsConstructor
    public static class Tag {
        private String key;
        private boolean isMeta;
        private Integer bojTagId;
    }
}
