package com.ssafy.dash.ai.infrastructure.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiCounterExampleResponse(
                @JsonProperty("input") String inputExample,
                @JsonProperty("expected") String expectedOutput,
                @JsonProperty("predicted") String predictedOutput,
                @JsonProperty("reason") String explanation) {
}
