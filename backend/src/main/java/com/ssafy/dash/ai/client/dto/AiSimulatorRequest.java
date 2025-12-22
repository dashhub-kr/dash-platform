package com.ssafy.dash.ai.client.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiSimulatorRequest {
    private String code;
    private String language;
}
