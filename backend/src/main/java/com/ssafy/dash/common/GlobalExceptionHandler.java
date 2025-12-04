package com.ssafy.dash.common;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.ssafy.dash.algorithm.domain.exception.AlgorithmRecordNotFoundException;
import com.ssafy.dash.board.domain.exception.BoardNotFoundException;
import com.ssafy.dash.common.exception.ApiErrorResponse;
import com.ssafy.dash.common.exception.ErrorCode;
import com.ssafy.dash.onboarding.domain.exception.WebhookRegistrationException;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({UserNotFoundException.class, BoardNotFoundException.class, AlgorithmRecordNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {

        return buildResponse(resolveNotFoundCode(ex), ex.getMessage(), request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiErrorResponse> handleValidation(BindException ex, HttpServletRequest request) {

        List<ApiErrorResponse.FieldErrorDetail> details = extractFieldErrors(ex.getBindingResult());
        log.warn("Validation failed: {}", details);
        return buildResponse(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getMessage(), request, details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        ApiErrorResponse.FieldErrorDetail detail = new ApiErrorResponse.FieldErrorDetail(
                ex.getName(),
                ex.getValue(),
                String.format("'%s' 파라미터 타입이 올바르지 않습니다.", ex.getName())
        );
        return buildResponse(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getMessage(), request, List.of(detail));
    }

    @ExceptionHandler(WebhookRegistrationException.class)
    public ResponseEntity<ApiErrorResponse> handleWebhook(WebhookRegistrationException ex, HttpServletRequest request) {

        log.warn("Webhook registration failed: {}", ex.getMessage());
        return buildResponse(ErrorCode.WEBHOOK_REGISTRATION_FAILED, ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {

        return buildResponse(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAll(Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception", ex);
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR, null, request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(ErrorCode errorCode, String message, HttpServletRequest request) {
        return buildResponse(errorCode, message, request, null);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(ErrorCode errorCode, String message, HttpServletRequest request, List<ApiErrorResponse.FieldErrorDetail> details) {
        ApiErrorResponse response = ApiErrorResponse.of(errorCode, message, request.getRequestURI(), details);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    private ErrorCode resolveNotFoundCode(RuntimeException ex) {
        if (ex instanceof UserNotFoundException) {
            return ErrorCode.USER_NOT_FOUND;
        }
        if (ex instanceof BoardNotFoundException) {
            return ErrorCode.BOARD_NOT_FOUND;
        }
        if (ex instanceof AlgorithmRecordNotFoundException) {
            return ErrorCode.ALGORITHM_RECORD_NOT_FOUND;
        }
        return ErrorCode.RESOURCE_NOT_FOUND;
    }

    private List<ApiErrorResponse.FieldErrorDetail> extractFieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(error -> new ApiErrorResponse.FieldErrorDetail(error.getField(), error.getRejectedValue(), error.getDefaultMessage()))
                .toList();
    }
    
}
