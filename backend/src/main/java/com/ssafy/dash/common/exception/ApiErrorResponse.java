package com.ssafy.dash.common.exception;

import java.time.Instant;
import java.util.List;

/**
 * REST API 예외 발생 시 반환되는 표준화된 에러 응답 객체
 * 
 * @param timestamp 에러 발생 시각
 * @param status HTTP 상태 코드
 * @param error HTTP 상태 메시지
 * @param code 플랫폼 내 에러 코드
 * @param message 에러 상세 메시지
 * @param path 요청 경로
 * @param details 필드별 검증 실패 정보
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldErrorDetail> details
) {

    public static ApiErrorResponse of(ErrorCode errorCode, String message, String path) {
        return of(errorCode, message, path, List.of());
    }

    public static ApiErrorResponse of(ErrorCode errorCode, String message, String path, List<FieldErrorDetail> details) {
        return new ApiErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getStatus().getReasonPhrase(),
                errorCode.getCode(),
                message != null ? message : errorCode.getMessage(),
                path,
                details == null ? List.of() : List.copyOf(details)
        );
    }

    /** 필드 단위 검증 실패 정보를 나타내는 구조체 */
    public record FieldErrorDetail(String field, Object rejectedValue, String reason) { }
    
}
