package com.example.matdongsanserver.domain.child.exception;

import com.example.matdongsanserver.common.exception.BusinessException;
import lombok.Getter;

@Getter
public class ChildException extends BusinessException {
    private final ChildErrorCode childErrorCode;

    public ChildException(ChildErrorCode childErrorCode) {
        super(childErrorCode);
        this.childErrorCode = childErrorCode;
    }
}
