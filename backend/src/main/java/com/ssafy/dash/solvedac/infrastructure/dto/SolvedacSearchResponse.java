package com.ssafy.dash.solvedac.infrastructure.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SolvedacSearchResponse {
    private Integer count;
    private List<ProblemItem> items;

    @Getter
    @Setter
    public static class ProblemItem {
        private Integer problemId;
        private String titleKo;
        private Integer level;
        private List<TagInfo> tags;
    }

    @Getter
    @Setter
    public static class TagInfo {
        private String key;
    }
}
