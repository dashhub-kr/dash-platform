package com.ssafy.dash.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 힌트 응답 DTO
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HintResponse {
    private int level;
    private String hint;
    private String encouragement;
    private List<String> relatedConcepts;
    private String nextStepSuggestion;
}
