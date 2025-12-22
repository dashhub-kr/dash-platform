package com.ssafy.dash.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 힌트 대화 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HintChatResponse {
    private String reply;

    @JsonProperty("teachingStyle")
    private String teachingStyle;

    @JsonProperty("followUpQuestions")
    private List<String> followUpQuestions;

    @JsonProperty("relatedConcepts")
    private List<String> relatedConcepts;

    private String encouragement;
}
