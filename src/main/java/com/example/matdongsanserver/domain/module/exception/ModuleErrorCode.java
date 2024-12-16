package com.example.matdongsanserver.domain.module.exception;

import com.example.matdongsanserver.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ModuleErrorCode implements ErrorCode {
    FAILED_TO_CONNECT_MODULE(HttpStatus.BAD_REQUEST, "인형 모듈 연결에 실패하였습니다"),
    INVALID_FILE(HttpStatus.BAD_REQUEST, "잘못된 음성 파일입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
