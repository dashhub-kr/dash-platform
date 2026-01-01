package com.ssafy.dash.analytics.application.dto.response;

import lombok.Data;

@Data
public class TagCoverageDto {
    private Integer totalCoreTags;
    private Integer solvedCoreTags;
    private Double coveragePercentage;
}
