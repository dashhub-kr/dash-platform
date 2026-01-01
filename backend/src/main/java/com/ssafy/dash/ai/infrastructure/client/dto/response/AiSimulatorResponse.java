package com.ssafy.dash.ai.infrastructure.client.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiSimulatorResponse {
    private String stdout;
    private String stderr;

    @JsonProperty("time_complexity")
    private String timeComplexity;

    @JsonProperty("space_complexity")
    private String spaceComplexity;

    private String analysis;
}
