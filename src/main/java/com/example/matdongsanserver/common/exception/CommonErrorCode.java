package com.example.matdongsanserver.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    S3_FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드에 실패하였습니다."),
    S3_URL_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 URL 이미지 업로드에 실패하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
