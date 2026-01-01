package com.ssafy.dash.ai.presentation.dto.request;

public record CodeReviewRequest(
        Long algorithmRecordId,
        String code,
        String language,
        String problemNumber,
        String platform,
        String problemTitle) {
}
