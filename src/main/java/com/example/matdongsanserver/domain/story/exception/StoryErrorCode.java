package com.example.matdongsanserver.domain.story.exception;

import com.example.matdongsanserver.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoryErrorCode implements ErrorCode {
    //동화 관련
    STORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "동화를 찾을 수 없습니다."),
    INVALID_AGE(HttpStatus.BAD_REQUEST, "유효하지 않은 나이 입력입니다."),
    INVALID_LANGUAGE(HttpStatus.BAD_REQUEST, "유효하지 않은 언어 입력입니다."),
    STORY_EDIT_PERMISSION_DENIED(HttpStatus.BAD_REQUEST, "동화 수정 권한이 없습니다."),
    LIKE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 좋아요하였습니다."),
    LIKE_NOT_EXISTS(HttpStatus.NOT_FOUND, "좋아요가 존재하지 않습니다."),

    //라이브러리 관련
    INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 정렬 타입입니다."),
    INVALID_LANGUAGE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 언어 타입입니다." ),
    INVALID_AGE_TYPE(HttpStatus.BAD_REQUEST,"유효하지 않은 레벨 유형입니다." );

    private final HttpStatus httpStatus;
    private final String message;
}
