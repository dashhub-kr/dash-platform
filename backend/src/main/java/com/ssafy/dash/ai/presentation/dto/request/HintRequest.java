package com.ssafy.dash.ai.presentation.dto.request;

public record HintRequest(
        Long userId,
        String problemNumber,
        String problemTitle,
        int level) {
}
