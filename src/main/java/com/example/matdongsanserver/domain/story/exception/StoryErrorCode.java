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
    TTS_GENERATION_FAILED(HttpStatus.BAD_REQUEST, "TTS 생성에 실패하였습니다."),
    INVALID_LANGUAGE_FOR_TRANSLATION(HttpStatus.BAD_REQUEST, "영어 동화만 번역 가능합니다."),
    STORY_EDIT_PERMISSION_DENIED(HttpStatus.BAD_REQUEST, "동화 수정 권한이 없습니다."),
    LIKE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 좋아요하였습니다."),
    LIKE_NOT_EXISTS(HttpStatus.NOT_FOUND, "좋아요가 존재하지 않습니다."),
    KOREAN_TTS_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "한글 TTS는 추후 지원  예정입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
