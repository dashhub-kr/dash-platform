package com.ssafy.dash.onboarding.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Solved.ac 핸들 검증 요청 DTO
 */
@Getter
@Setter
public class SolvedacVerifyRequest {
    @NotBlank(message = "핸들은 필수입니다")
    private String handle;
}
