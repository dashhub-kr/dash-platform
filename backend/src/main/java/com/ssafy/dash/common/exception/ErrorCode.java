package com.ssafy.dash.common.exception;

import org.springframework.http.HttpStatus;

/** 플랫폼 전체 에러 메타데이터 레지스트리 */
public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "요청한 사용자를 찾을 수 없습니다."),
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_NOT_FOUND", "요청한 게시글을 찾을 수 없습니다."),
    ALGORITHM_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "ALGORITHM_RECORD_NOT_FOUND", "요청한 알고리즘 기록을 찾을 수 없습니다."),
    WEBHOOK_REGISTRATION_FAILED(HttpStatus.BAD_GATEWAY, "WEBHOOK_REGISTRATION_FAILED", "GitHub 웹훅 등록에 실패했습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "요청 값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "예상치 못한 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
    
}
