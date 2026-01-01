package com.ssafy.dash.ai.infrastructure.client.dto.request;

import lombok.Builder;
import lombok.Getter;

/**
 * 코드 분석 요청 DTO
 */
@Getter
@Builder
public class CodeReviewRequest {
    private String code;
    private String language;
    private String problemNumber;
    private String platform;
    private String problemTitle;
}
