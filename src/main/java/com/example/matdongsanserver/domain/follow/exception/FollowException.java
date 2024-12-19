package com.example.matdongsanserver.domain.follow.exception;

import com.example.matdongsanserver.common.exception.BusinessException;
import lombok.Getter;

@Getter
public class FollowException extends BusinessException {
    private final FollowErrorCode followErrorCode;

    public FollowException(FollowErrorCode followErrorCode) {
        super(followErrorCode);
        this.followErrorCode = followErrorCode;
    }
}
