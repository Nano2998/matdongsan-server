package com.example.matdongsanserver.domain.story.exception;

import com.example.matdongsanserver.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoryErrorCode implements ErrorCode {
    STORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "동화를 찾을 수 없습니다."),
    INVALID_AGE(HttpStatus.BAD_REQUEST, "유효하지 않은 나이 입력입니다."),
    INVALID_LANGUAGE(HttpStatus.BAD_REQUEST, "유효하지 않은 언어 입력입니다."),
    STORY_GENERATION_FAILED(HttpStatus.BAD_REQUEST, "동화 생성에 실패하였습니다."),
    STORY_TRANSLATION_FAILED(HttpStatus.BAD_REQUEST, "동화 번역에 실패하였습니다."),
    INVALID_LANGUAGE_FOR_TRANSLATION(HttpStatus.BAD_REQUEST, "영어 동화만 번역 가능합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
