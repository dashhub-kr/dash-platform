package com.ssafy.dash.ai.infrastructure.client.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiCounterExampleRequest(
        Long recordId,
        @JsonProperty("problemNumber") String problemNumber,
        String code,
        String language,
        String platform,
        @JsonProperty("problemTitle") String problemTitle,
        @JsonProperty("problemDescription") String problemDescription) {
}
