package com.example.matdongsanserver.domain.member.exception;

import com.example.matdongsanserver.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    CHILD_NOT_FOUND(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다."),
    FOLLOW_ALREADY_EXISTS(HttpStatus.NOT_FOUND, "이미 팔로우하였습니다."),
    FOLLOW_NOT_EXISTS(HttpStatus.NOT_FOUND, "팔로우가 존재하지 않습니다."),
    PROFILE_IMAGE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "프로필 이미지 업로드에 실패하였습니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "유효하지 않은 파일명입니다."),
    INVALID_AGE(HttpStatus.BAD_REQUEST, "유효하지 않은 나이입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
