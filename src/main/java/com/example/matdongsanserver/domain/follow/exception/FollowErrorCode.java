package com.example.matdongsanserver.domain.follow.exception;

import com.example.matdongsanserver.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FollowErrorCode implements ErrorCode {
    FOLLOW_ALREADY_EXISTS(HttpStatus.NOT_FOUND, "이미 팔로우하였습니다."),
    FOLLOW_NOT_EXISTS(HttpStatus.NOT_FOUND, "팔로우가 존재하지 않습니다."),
    CANNOT_FOLLOW_MYSELF(HttpStatus.BAD_REQUEST, "본인은 팔로우할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
