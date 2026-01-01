package com.ssafy.dash.analytics.application.dto.response;

import lombok.Data;

@Data
public class FamilyScoreDto {
    private Long familyId;
    private String familyName;
    private String familyKey;
    private Double rawScore;
    private Integer distinctTags;
    private Integer solvedProblems;
}
