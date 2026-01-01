package com.ssafy.dash.solvedac.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Solved.ac 클래스별 통계 응답 DTO
 */
@Getter
@Setter
public class ClassStatResponse {
    @JsonProperty("class")
    private Integer classNumber;

    private Integer total;

    private Integer totalSolved;

    private Integer essentials;

    private Integer essentialSolved;

    private String decoration;
}
