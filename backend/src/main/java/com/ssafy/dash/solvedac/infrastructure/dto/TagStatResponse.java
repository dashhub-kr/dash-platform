package com.ssafy.dash.solvedac.infrastructure.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * Solved.ac 태그별 통계 응답 DTO
 */
@Getter
@Setter
public class TagStatResponse {
    private Integer count;
    private List<TagStatItem> items;

    @Getter
    @Setter
    public static class TagStatItem {
        private TagInfo tag;
        private Integer total;
        private Integer solved;
        private Integer partial;
        private Integer tried;
    }

    @Getter
    @Setter
    public static class TagInfo {
        private String key;
        private Boolean isMeta;
        private Integer bojTagId;
    }
}
