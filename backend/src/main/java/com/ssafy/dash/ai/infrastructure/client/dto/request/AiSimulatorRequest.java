package com.ssafy.dash.ai.infrastructure.client.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiSimulatorRequest {
    private String code;
    private String language;
}
