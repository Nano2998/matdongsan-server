package com.example.matdongsanserver.domain.member.exception;

import com.example.matdongsanserver.common.exception.BusinessException;
import lombok.Getter;

@Getter
public class MemberException extends BusinessException {
    private final MemberErrorCode memberErrorCode;

    public MemberException(MemberErrorCode memberErrorCode) {
        super(memberErrorCode);
        this.memberErrorCode = memberErrorCode;
    }
}
