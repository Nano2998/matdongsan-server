package com.example.matdongsanserver.domain.child.exception;

import com.example.matdongsanserver.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChildErrorCode  implements ErrorCode {
    CHILD_NOT_FOUND(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다."),
    INVALID_AGE(HttpStatus.BAD_REQUEST, "유효하지 않은 나이입니다."),
    CANNOT_ACCESS_CHILD(HttpStatus.BAD_REQUEST, "자녀를 수정, 삭제할 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
