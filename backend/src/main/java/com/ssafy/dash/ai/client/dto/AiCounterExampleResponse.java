package com.ssafy.dash.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiCounterExampleResponse(
                @JsonProperty("input") String inputExample,
                @JsonProperty("expected") String expectedOutput,
                @JsonProperty("predicted") String predictedOutput,
                @JsonProperty("reason") String explanation) {
}
