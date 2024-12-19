package com.example.matdongsanserver.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    //JSON 관련
    JSON_PARSING_ERROR(HttpStatus.BAD_REQUEST, "JSON 형식으로 파싱할 수 없습니다."),

    //S3 관련
    S3_FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드에 실패하였습니다."),
    S3_URL_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 URL 이미지 업로드에 실패하였습니다."),

    //외부 API 관련
    STORY_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "동화 생성에 실패하였습니다."),
    STORY_SUMMARY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "동화 요약에 실패하였습니다."),
    QUESTION_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "질문 생성에 실패하였습니다."),
    TTS_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TTS 생성에 실패하였습니다."),
    STT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STT 생성에 실패하였습니다."),
    STORY_IMAGE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 생성 오류입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
