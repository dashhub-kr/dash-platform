package com.ssafy.dash.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiCounterExampleRequest(
        @JsonProperty("problemNumber") String problemNumber,
        String code,
        String language,
        @JsonProperty("problemDescription") String problemDescription) {
}
