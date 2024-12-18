package com.example.matdongsanserver.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException occurred: {}, ErrorCode: {}", e.getErrorCode().getMessage(), e.getErrorCode(), e);
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected Exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(500)
                .body(ErrorResponse.builder()
                        .status(500)
                        .code("SERVER_ERROR")
                        .message("Unexpected error occurred.")
                        .build());
    }
}
