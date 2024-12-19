package com.example.matdongsanserver.domain.member.exception;

import com.example.matdongsanserver.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    PROFILE_IMAGE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "프로필 이미지 업로드에 실패하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
