package com.ssafy.dash.solvedac.infrastructure.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SolvedacTagRatingResponse {
    private Integer count;
    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private TagInfo tag;
        private Integer rating;
        private Integer solvedCount;
    }

    @Getter
    @Setter
    public static class TagInfo {
        private String key;
        private Boolean isMeta;
        private Integer bojTagId;
    }
}
