package com.example.matdongsanserver.domain.auth.exception;

import com.example.matdongsanserver.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요한 요청입니다."),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "접근 불가능한 권한입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
