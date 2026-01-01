package com.ssafy.dash.ai.presentation.dto.request;

import java.util.List;

public record UserContextDto(String tierName, int solvedCount, List<String> weakTags) {
}
