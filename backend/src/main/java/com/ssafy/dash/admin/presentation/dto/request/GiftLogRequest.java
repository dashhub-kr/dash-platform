package com.ssafy.dash.admin.presentation.dto.request;

public record GiftLogRequest(
                Long userId,
                Integer amount,
                String reason) {
        public GiftLogRequest {
                if (userId == null) {
                        throw new IllegalArgumentException("userId must not be null");
                }
                if (amount == null || amount <= 0) {
                        throw new IllegalArgumentException("amount must be a positive integer");
                }
                if (reason == null || reason.isBlank()) {
                        throw new IllegalArgumentException("reason must not be null or blank");
                }
        }
}
