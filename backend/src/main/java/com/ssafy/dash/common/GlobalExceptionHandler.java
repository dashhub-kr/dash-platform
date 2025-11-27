package com.ssafy.dash.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ssafy.dash.algorithm.exception.AlgorithmRecordNotFoundException;
import com.ssafy.dash.board.exception.BoardNotFoundException;
import com.ssafy.dash.onboarding.exception.WebhookRegistrationException;
import com.ssafy.dash.user.exception.UserNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, BoardNotFoundException.class, AlgorithmRecordNotFoundException.class})
    public ResponseEntity<String> handleNotFound(RuntimeException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(WebhookRegistrationException.class)
    public ResponseEntity<String> handleWebhook(WebhookRegistrationException ex) {

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception ex) {
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

}
