package com.ssafy.dash.problem.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Tag {
    private Long id;
    private String tagKey;
    private Integer bojTagId;
    private Long familyId;
    private String parentTagKey;
    private String importanceTier;
    private Double weight;
    private Boolean isCore;
    private String displayNames; // stored as JSON string

    private Tag(String tagKey, Integer bojTagId, Long familyId, String parentTagKey, String importanceTier,
            Double weight, Boolean isCore, String displayNames) {
        this.tagKey = tagKey;
        this.bojTagId = bojTagId;
        this.familyId = familyId;
        this.parentTagKey = parentTagKey;
        this.importanceTier = importanceTier;
        this.weight = weight;
        this.isCore = isCore;
        this.displayNames = displayNames;
    }

    public static Tag create(String tagKey, Integer bojTagId, Long familyId, String parentTagKey, String importanceTier,
            Double weight, Boolean isCore, String displayNames) {
        return new Tag(tagKey, bojTagId, familyId, parentTagKey, importanceTier, weight, isCore, displayNames);
    }
}
