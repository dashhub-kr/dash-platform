package com.ssafy.dash.admin.presentation.dto.request;

public record GiftAcornRequest(
    Long studyId,
    Integer amount,
    String reason
) {
}
