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

    /**
     * displayNames JSON에서 한국어 이름 추출
     * 형식 예: [{"language":"ko","name":"이분 탐색"},{"language":"en","name":"Binary
     * Search"}]
     */
    public String getKoreanName() {
        if (displayNames == null || displayNames.isEmpty()) {
            return tagKey;
        }
        try {
            // 간단한 정규식으로 ko 언어 이름 추출
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    "\"language\"\\s*:\\s*\"ko\"[^}]*\"name\"\\s*:\\s*\"([^\"]+)\"");
            java.util.regex.Matcher matcher = pattern.matcher(displayNames);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // 파싱 실패 시 tagKey 반환
        }
        return tagKey;
    }
}
