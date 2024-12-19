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
        log.warn("[맛동산]: 예외 발생, 예외내용 = {}, 예외 코드 = {}", e.getErrorCode().getMessage(), e.getErrorCode());
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("[맛동산]: 예상치 못한 예외 발생, 예외내용 = {}", e.getMessage());
        return ResponseEntity.status(500)
                .body(ErrorResponse.builder()
                        .status(500)
                        .code("SERVER_ERROR")
                        .message(e.getMessage())
                        .build());
    }
}
