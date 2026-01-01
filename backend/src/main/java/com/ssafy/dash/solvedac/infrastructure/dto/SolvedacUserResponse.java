package com.ssafy.dash.solvedac.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Solved.ac 사용자 기본 정보 응답 DTO
 */
@Getter
@Setter
public class SolvedacUserResponse {
    private String handle;

    private String bio;

    private Integer tier;

    private Integer rating;

    @JsonProperty("class")
    private Integer classLevel;

    private Integer maxStreak;

    private String profileImageUrl;

    private Integer solvedCount;
}
