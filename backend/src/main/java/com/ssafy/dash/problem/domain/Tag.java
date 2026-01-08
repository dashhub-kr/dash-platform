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
    private Boolean isBasic;
    private String displayNames; // stored as JSON string

    private Tag(String tagKey, Integer bojTagId, Long familyId, String parentTagKey, String importanceTier,
            Double weight, Boolean isCore, Boolean isBasic, String displayNames) {
        this.tagKey = tagKey;
        this.bojTagId = bojTagId;
        this.familyId = familyId;
        this.parentTagKey = parentTagKey;
        this.importanceTier = importanceTier;
        this.weight = weight;
        this.isCore = isCore;
        this.isBasic = isBasic;
        this.displayNames = displayNames;
    }

    public static Tag create(String tagKey, Integer bojTagId, Long familyId, String parentTagKey, String importanceTier,
            Double weight, Boolean isCore, Boolean isBasic, String displayNames) {
        return new Tag(tagKey, bojTagId, familyId, parentTagKey, importanceTier, weight, isCore, isBasic, displayNames);
    }

    /**
     * displayNames JSON에서 한국어 이름 추출
     * 형식 예: [{"language":"ko","name":"이분 탐색"},{"language":"en","name":"Binary
     * Search"}]
     */
    public String getKoreanName() {
        if (displayNames == null || displayNames.isEmpty()) {
            return tagKey;
        }
        // TagService에서 이미 한국어 이름만 추출해서 저장하므로 그대로 반환
        return displayNames;
    }
}
