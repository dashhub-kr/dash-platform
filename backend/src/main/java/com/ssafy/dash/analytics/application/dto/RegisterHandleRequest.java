package com.ssafy.dash.analytics.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solved.ac 핸들 등록 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterHandleRequest {
    private String handle;
}
