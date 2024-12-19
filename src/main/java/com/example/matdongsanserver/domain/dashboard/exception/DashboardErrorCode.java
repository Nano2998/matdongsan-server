package com.example.matdongsanserver.domain.dashboard.exception;

import com.example.matdongsanserver.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DashboardErrorCode implements ErrorCode {
    STORY_QUESTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "동화 질문을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
